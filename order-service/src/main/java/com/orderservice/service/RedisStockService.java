package com.orderservice.service;

import com.common.exception.BaseBizException;
import com.orderservice.client.ProductServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStockService {

    private final StringRedisTemplate redisTemplate;
    private final ProductServiceClient productServiceClient;
    private final RedissonClient redissonClient;

    private static final String STOCK_CACHE_KEY_PREFIX = "stock:";

    //상품 재고 조회 (캐시 미스이면, DB 조회)
    public Integer getProductStock(Long productId) {

        String key = STOCK_CACHE_KEY_PREFIX + productId;

        // 캐시에서 재고 조회
        String cachedStock = redisTemplate.opsForValue().get(key);
        if (cachedStock != null) {
            log.info("캐시에서 상품 {} 재고 조회: {}", productId, cachedStock);
            return Integer.parseInt(cachedStock);
        }

        // 캐시에 없으면 데이터베이스 조회
        Integer stock = productServiceClient.getProductStock(productId).getStock();

        //캐시에 저장
        redisTemplate.opsForValue().set(key, String.valueOf(stock), 30, TimeUnit.MINUTES);

        return stock;
    }

    //재고 감소
    public void decreaseStockWithLock(Long productId, int quantity) {

        String lockKey = "lock:" + productId.toString(); // 락 키
        RLock lock = redissonClient.getLock(lockKey);

        try {

            //락 획득 시도
            boolean available = lock.tryLock(10, 5, TimeUnit.SECONDS);
            if (!available) {
                log.warn("Lock 획득 실패: productId={}", productId);
            }

            log.warn("Lock 획득 성공: productId={}", productId);

            //재고 감소
            String key = STOCK_CACHE_KEY_PREFIX + productId;
            int currentStock = getProductStock(productId);

            if (currentStock < quantity) {
                throw new BaseBizException("재고 부족으로 주문에 실패했습니다.");
            }

            redisTemplate.opsForValue().set(key, String.valueOf(currentStock - quantity));
            log.info("상품 ID {} 재고 감소 {}->{} ", productId, currentStock, currentStock - quantity);

        } catch (InterruptedException e) {
            log.error("Lock 획득 중 오류 발생: productId={}", quantity, e);
            Thread.currentThread().interrupt(); // 인터럽트 상태 복구

        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) { //락을 점유한 스레드만 해제 가능
                log.info("Lock 해제: productId={}", productId);
                lock.unlock();
            }
        }
    }

    // 상품 재고 증가
    @Transactional
    public void increaseStock(Long productId, int quantity) {
        String cacheKey = STOCK_CACHE_KEY_PREFIX + productId;
        int currentStock = getProductStock(productId);
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(currentStock + quantity));
        log.info("상품 {} 재고가 {} 증가되었습니다. 현재 재고: {}", productId, quantity, currentStock + quantity);
    }
}
