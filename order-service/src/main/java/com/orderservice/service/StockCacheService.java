package com.orderservice.service;

import com.common.exception.BaseBizException;
import com.orderservice.client.ProductServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ProductServiceClient productServiceClient;

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
        redisTemplate.opsForValue().set(key, String.valueOf(stock),30, TimeUnit.MINUTES);

        return stock;
    }

    // 상품 재고 감소
    @Transactional
    public void decreaseStock(Long productId, int quantity) {

        String cacheKey = STOCK_CACHE_KEY_PREFIX + productId;
        int currentStock = getProductStock(productId);

        if (currentStock >= quantity) {
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(currentStock - quantity));
            log.info("상품 {} 재고가 {} 감소되었습니다. 현재 재고: {}", productId, quantity, currentStock - quantity);
        } else {
            throw new BaseBizException("재고 부족으로 주문에 실패했습니다.");
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
