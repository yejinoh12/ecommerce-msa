package com.productservice.redis;

import com.common.dto.order.UpdateStockReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonLock {

    private final RedissonClient redissonClient;
    private final RedisStockService redisStockService;

    public void updateStockRedisson(List<UpdateStockReqDto> updateStockReqDtos) {

        for (UpdateStockReqDto dto : updateStockReqDtos) {

            Long productId = dto.getProductId();
            String lockKey = "lock:product:" + productId.toString(); // 락 키
            RLock lock = redissonClient.getFairLock(lockKey);

            try {
                //40s 동안 1s 간격으로 Lock 시도
                boolean available = lock.tryLock(40, 1, TimeUnit.SECONDS);
                if (!available) {
                    log.warn("Lock 획득 실패: productId={}", productId);
                    continue;
                }

                log.warn("Lock 획득 성공: productId={}", productId);

                redisStockService.decrementStock(productId, dto.getCnt());

            } catch (InterruptedException e) {
                log.error("Lock 획득 중 오류 발생: productId={}", dto.getProductId(), e);
                Thread.currentThread().interrupt(); // 인터럽트 상태 복구

            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) { //락을 점유한 스레드만 해제 가능
                    log.info("Lock 해제: productId={}", productId);
                    lock.unlock();
                }
            }
        }
    }
}
