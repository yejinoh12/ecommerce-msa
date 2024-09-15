package com.productservice.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisStockRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String STOCK_KEY_PREFIX = "product:";


    // 저장
    public void save(Long productId, int quantity, LocalDateTime eventStartTime, LocalDateTime eventEndTime) {

        Map<String, String> stockData = new HashMap<>();
        stockData.put("stock", Integer.toString(quantity));
        stockData.put("start", eventStartTime.format(RedisProduct.formatter));
        stockData.put("end", eventEndTime.format(RedisProduct.formatter));

        redisTemplate.opsForHash().putAll(STOCK_KEY_PREFIX + productId, stockData);
    }

    // 재고 감소
    public void decrementStock(Long productId, int stock) {
        String key = STOCK_KEY_PREFIX + productId;
        redisTemplate.opsForHash().increment(key, "stock", -stock);
    }

    // 재고 증가
    public void incrementStock(Long productId, int stock) {
        String key = STOCK_KEY_PREFIX + productId;
        redisTemplate.opsForHash().increment(key, "stock", stock);
    }

    // 삭제
    public void delete(Long productId) {
        redisTemplate.delete(STOCK_KEY_PREFIX + productId);
    }

    // 조회
    public Optional<RedisProduct> findProductStock(Long productId) {
        String key = STOCK_KEY_PREFIX + productId;
        Map<Object, Object> stockData = redisTemplate.opsForHash().entries(key);

        if (stockData.isEmpty()) {
            return Optional.empty();
        }

        Integer quantity = Integer.parseInt((String) stockData.get("stock"));
        LocalDateTime eventStartTime = LocalDateTime.parse((String) stockData.get("start"), RedisProduct.formatter);
        LocalDateTime eventEndTime = LocalDateTime.parse((String) stockData.get("end"), RedisProduct.formatter);

        return Optional.of(new RedisProduct(quantity, eventStartTime, eventEndTime));
    }
}
