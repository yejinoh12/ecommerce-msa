package com.orderservice.service;

import com.common.dto.product.StockResponse;
import com.common.exception.BaseBizException;
import com.orderservice.client.ProductServiceClient;
import com.orderservice.repository.RedisStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStockService {

    private final RedisStockRepository redisStockRepository;
    private final ProductServiceClient productServiceClient;

    @Transactional
    public void enrollStock(Long productId, int quantity) {
        redisStockRepository.save(productId, quantity);
    }

    @Transactional
    public void purchase(Long productId, Integer quantity) {

        log.info("redis 재고 감소 요청");

        Integer currentStock = findProductStock(productId);
        if(currentStock == null){ //재고가 없다면 재고 정보 요청
            log.info("레디스에 재고 정보 없음, 상푸 서비스에 재고 정보 조회");
            StockResponse stockResponse = productServiceClient.getProductStock(productId);
            currentStock = stockResponse.getStock();
            enrollStock(productId, currentStock); //재고 정보를 레디스에 저장
        }

        if (currentStock < quantity) {
            throw new BaseBizException("재고가 부족합니다.");
        }

        log.info("redis 재고 감소 시작, productId = {}, stock = {} ", productId, currentStock);
        redisStockRepository.decrementStock(productId, quantity);
        log.info("redis 재고 감소 완료, productId = {}, stock = {}", productId, currentStock - quantity);
    }

    @Transactional
    public void cancel(Long productId, int quantity) {
        log.info("redis 재고 증가 시작, productId = {}, stock = {} ", productId, quantity);
        redisStockRepository.incrementStock(productId, quantity);
        log.info("redis 재고 증가 완료, productId = {}", productId);
    }

    @Transactional
    public Integer findProductStock(Long productId) {
        return redisStockRepository.findProductStock(productId).orElse(null);
    }

    @Transactional
    public void deleteStock(Long productId) {
        redisStockRepository.delete(productId);
    }
}