package com.productservice.service.stock;

import com.common.dto.order.UpdateStockReqDto;
import com.productservice.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStockLockService {

    private final RedissonClient redissonClient;
    private final StockService stockService;
//    private final RedisStockService redisStockService;
//    private final ProductService productService;

    //재고 감소
    public void updateStockRedisson(List<UpdateStockReqDto> updateStockReqDtos) {

        for (UpdateStockReqDto dto : updateStockReqDtos) {

            Long productId = dto.getProductId();
            String key = productId.toString();
            RLock lock = redissonClient.getLock(key);

            try {

                boolean available = lock.tryLock(40, 1, TimeUnit.SECONDS); //40s 동안 1s 간격으로 Lock 시도
                if (!available) {
                    log.warn("Lock 획득 실패: productId={}", productId);
                    continue;
                }

                log.warn("Lock 획득 성공: productId={}", productId);
                //stockService.decrease(productId, dto.getCnt());

                handleStockUpdate(productId, dto.getCnt(), dto.getAction()); //변경이 일어나는 부분

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

    public void handleStockUpdate(Long productId, int quantity, String action) {

        switch (action) {
            case "INC":
                stockService.increase(productId, quantity);
                //redisStockService.cancel(productId, quantity);
                //stockService.synchronizeStock(productId);
                break;

            case "DEC":
                stockService.decrease(productId, quantity);
                //redisStockService.purchase(productId, quantity);
                break;

//            case "SYNC":
//                stockService.synchronizeStock(productId);
//                break;

            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }

//        Integer currentStock = redisStockService.loadDataIfAbsentInCache(productId);
//        int dbStock = productService.findProductById(productId).getStock();
//        log.info("재고 변경 로직 완료, redis 재고  = {}, db 재고 = {} ", currentStock, dbStock);
    }
}
