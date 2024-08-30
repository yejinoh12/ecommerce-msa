package com.productservice.service.stock;

import com.common.dto.order.UpdateStockReqDto;
import com.productservice.domain.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStockLockService {

    private final RedissonClient redissonClient;
    private final RedisStockService redisStockService;
    private final StockService stockService;

    //재고 감소
    @Transactional
    public void updateStockRedisson(List<UpdateStockReqDto> updateStockReqDtos) {

        log.info("재고 변경 로직 시작, Type = {}", updateStockReqDtos.get(0).getAction());

        for (UpdateStockReqDto dto : updateStockReqDtos) {

            Long productId = dto.getProductId();
            int cnt = dto.getCnt();
            String key = productId.toString();
            RLock lock = redissonClient.getLock(key);

            try {

                boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS); //40s 동안 1s 간격으로 Lock 시도
                if (!available) {
                    log.warn("Lock 획득 실패: productId={}", productId);
                    continue;
                }

                handleStockUpdate(productId, cnt, dto.getAction());

            } catch (InterruptedException e) {

                log.error("Lock 획득 중 오류 발생: productId={}", dto.getProductId(), e);
                Thread.currentThread().interrupt(); // 인터럽트 상태 복구

            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) { //락을 점유한 스레드만 해제 가능
                    lock.unlock();
                    log.info("Lock 해제: productId={}", productId);
                }
            }
        }
    }

    @Transactional
    public void handleStockUpdate(Long productId, int quantity, String action) {

        Integer stock = redisStockService.loadDataIfAbsentInCache(productId);
        log.info("조회된 재고 = {}", stock);

        switch (action) {

            case "INC":
                redisStockService.cancel(productId, quantity);
                stockService.synchronizeStock(productId);
                break;

            case "DEC":
                redisStockService.purchase(productId, quantity);
                break;

            case "SYNC":
                stockService.synchronizeStock(productId);
                break;

            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }
    }
}
