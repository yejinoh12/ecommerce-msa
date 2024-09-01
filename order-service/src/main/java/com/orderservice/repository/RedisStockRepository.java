package com.orderservice.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStockRepository {

    private final RedisTemplate<String, Integer> redisTemplate;
    private static final Duration REDIS_STOCK_CACHE_TTL = Duration.ofHours(1); // TTL 1시간

    public void save(Long productId, Integer quantity) {
        String key = getKey(productId);
        log.info("Setting stock in Redis: {} with value {}", key, quantity);
        redisTemplate.opsForValue().set(key, quantity, REDIS_STOCK_CACHE_TTL);
    }

    public void decrementStock(Long productId, Integer quantity) {
        String key = getKey(productId);
        redisTemplate.opsForValue().decrement(key, quantity);
    }

    public void incrementStock(Long productId, Integer quantity) {
        String key = getKey(productId);
        redisTemplate.opsForValue().increment(key, quantity);
    }

    public Optional<Integer> findProductStock(Long productId) {
        String key = getKey(productId);
        Integer stock = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(stock);
    }

    public void delete(Long productId) {
        redisTemplate.delete(productId.toString());
    }

    private String getKey(Long productId) {
        return "" + productId;
    }

}
