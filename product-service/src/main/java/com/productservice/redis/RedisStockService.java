package com.productservice.redis;

import com.common.exception.BaseBizException;
import com.productservice.domain.Product;
import com.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisStockService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProductRepository productRepository;
    private static final String STOCK_KEY_PREFIX = "product:";

    // 저장
    public void save(Long productId, int stock) {
        String key = STOCK_KEY_PREFIX + productId;
        redisTemplate.opsForValue().set(key, String.valueOf(stock));
    }

    // 재고 감소
    public void decrementStock(Long productId, int stock) {

        String key = STOCK_KEY_PREFIX + productId;

        int currentStock = getStock(productId);

        // 재고가 충분한지 확인 (Read)
        if (currentStock < stock) {
            throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + currentStock);
        }

        // 재고 감소
        redisTemplate.opsForValue().decrement(key, stock);

        log.info("상품 ID: {}, 재고가 {}만큼 감소했습니다. 현재 재고: {}", productId, stock, currentStock - stock);
    }

    // 재고 증가
    public void incrementStock(Long productId, int stock) {
        String key = STOCK_KEY_PREFIX + productId;
        redisTemplate.opsForValue().increment(key, stock);
    }

    // 삭제
    public void delete(Long productId) {
        redisTemplate.delete(STOCK_KEY_PREFIX + productId);
    }

    // 레디스에 객체가 있는지 확인
    public Optional<Integer> productExistsInRedis(String key) {

        String stockValue = redisTemplate.opsForValue().get(key);

        if (stockValue == null) {
            return Optional.empty();
        }

        Integer stock = Integer.parseInt(stockValue);
        return Optional.of(stock);
    }

    // 레디스에 저장되어 있지 않다면 캐싱해서 반환
    public int getStock(Long productId) {

        String key = STOCK_KEY_PREFIX + productId;
        return productExistsInRedis(key).orElseGet(() -> {

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new BaseBizException("제품을 찾을 수 없습니다. 상품 ID :" + productId));

            int stock = product.getStock();
            save(productId, stock);

            log.info("[{}] Redis 캐시를 업데이트했습니다, productId = {}, 수량 = {}", LocalDateTime.now(), productId, stock);

            return stock;
        });
    }
}
