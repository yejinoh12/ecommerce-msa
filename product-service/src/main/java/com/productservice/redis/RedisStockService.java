package com.productservice.redis;

import com.common.exception.BaseBizException;
import com.productservice.domain.Product;
import com.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStockService {

    private final RedisStockRepository redisStockRepository;
    private final ProductRepository productRepository;

    public void enrollStock(Long productId, int quantity, LocalDateTime eventStartTime, LocalDateTime eventEndTime) {
        redisStockRepository.save(productId, quantity, eventStartTime, eventEndTime);
        log.info("[{}] Redis 재고 등록 완료, productId = {}", LocalDateTime.now(), productId);
    }

    @Transactional
    public void purchase(Long productId, int quantity) {

        RedisProduct redisProduct = loadDataIfAbsentInCache(productId);

        //구매 가능 여부 확인
        redisProduct.canPurchase(quantity);

        // 재고 부족 검증
        if (redisProduct.getStock() < quantity) {
            throw new BaseBizException("재고가 부족합니다.");
        }

        log.info("[{}] Redis 재고 감소 시작, productId = {}, 현재 재고 = {}", LocalDateTime.now(), productId, redisProduct.getStock());
        redisStockRepository.decrementStock(productId, quantity);
        log.info("[{}] Redis 재고 감소 완료, productId = {}, 남은 재고 = {}", LocalDateTime.now(), productId, redisProduct.getStock() - quantity);
    }

    @Transactional
    public void cancel(Long productId, int quantity) {

        RedisProduct redisProduct = loadDataIfAbsentInCache(productId);
        Integer currentStock = redisProduct.getStock();

        log.info("[{}] Redis 재고 증가 시작, productId = {}, 현재 재고 = {}", LocalDateTime.now(), productId, currentStock);
        redisStockRepository.incrementStock(productId, quantity);
        log.info("[{}] Redis 재고 증가 완료, productId = {}, 증가된 재고 = {}", LocalDateTime.now(), productId, currentStock + quantity);
    }

    @Transactional
    public void deleteStock(Long productId) {
        redisStockRepository.delete(productId);
        log.info("[{}] Redis 에서 상품 재고 삭제, productId = {}", LocalDateTime.now(), productId);
    }

    // 캐시에서 현재 재고를 로드하거나, 없으면 데이터베이스에서 로드
    public RedisProduct loadDataIfAbsentInCache(Long productId) {

        Optional<RedisProduct> optionalProduct = redisStockRepository.findProductStock(productId);

        if (optionalProduct.isEmpty()) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new BaseBizException("제품을 찾을 수 없습니다."));

            Integer stock = product.getStock();
            LocalDateTime eventStartTime = product.getEventStartTime();
            LocalDateTime eventEndTime = product.getEventEndTime();
            enrollStock(productId, stock, eventStartTime, eventEndTime);

            log.info("[{}] Redis 캐시를 업데이트했습니다, productId = {}, 수량 = {}", LocalDateTime.now(), productId, stock);
            return new RedisProduct(stock, eventStartTime, eventEndTime);
        } else {
            log.info("Redis 재고 조회");
            return optionalProduct.get();
        }
    }
}
