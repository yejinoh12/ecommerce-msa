package com.productservice.service;

import com.common.dto.product.StockResponse;
import com.productservice.domain.Product;
import com.productservice.redis.RedisProduct;
import com.productservice.redis.RedisStockService;
import com.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;
    private final RedisStockService redisStockService;

    public StockResponse getProductStock(Long productId) {
        RedisProduct product = redisStockService.loadDataIfAbsentInCache(productId);
        return new StockResponse(productId, product.getStock(),
                LocalDateTime.parse(product.getEventStartTime()), LocalDateTime.parse(product.getEventEndTime()));
    }

    @Transactional
    public void handleStockUpdate(Long productId, int quantity, String action) {

        switch (action) {
            case "RI":
                redisStockService.cancel(productId, quantity);
                break;

            case "RD":
                redisStockService.purchase(productId, quantity);
                break;

            case "INC":
                redisStockService.cancel(productId, quantity);
                increase(productId,quantity);
                break;

            case "DEC":
                decrease(productId, quantity);
                break;

            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrease(Long productId, int quantity) {
        log.info("db 재고 감소, productId = {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. productId=" + productId));
        product.decreaseStock(quantity);
        productRepository.saveAndFlush(product);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increase(Long productId, int quantity) {
        log.info("db 재고 증가, productId = {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. productId=" + productId));
        product.increaseStock(quantity);
        productRepository.saveAndFlush(product);
    }
}
