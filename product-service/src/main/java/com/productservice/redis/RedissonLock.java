package com.productservice.redis;

import com.common.dto.order.UpdateStockReqDto;
import com.productservice.service.StockService;
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
    private final StockService stockService;

    //재고 감소
    public void updateStockRedisson(List<UpdateStockReqDto> updateStockReqDtos, String action) {

        for (UpdateStockReqDto dto : updateStockReqDtos) {

            Long productId = dto.getProductId();
            String lockKey = "lock:product:" + productId.toString(); // 락 키
            RLock lock = redissonClient.getLock(lockKey);

            try {
                boolean available = lock.tryLock(40, 1, TimeUnit.SECONDS); //40s 동안 1s 간격으로 Lock 시도
                if (!available) {
                    log.warn("Lock 획득 실패: productId={}", productId);
                    continue;
                }

                log.warn("Lock 획득 성공: productId={}", productId);
                stockService.handleStockUpdate(productId, dto.getCnt(), action); //변경이 일어나는 부분

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
