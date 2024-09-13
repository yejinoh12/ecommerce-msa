
package com.productservice.service;

import com.common.dto.order.UpdateStockReqDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.productservice.domain.Product;
import com.productservice.dto.product.ProductStockResDto;
import com.productservice.redis.RedisStockService;
import com.productservice.redis.RedissonLock;
import com.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;
    private final RedisStockService redisStockService;
    private final RedissonLock redissonLock;

    //재고 조회
    public ApiResponse<ProductStockResDto> getProductStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BaseBizException("상품을 찾을 수 없습니다. ID: " + productId));
        return ApiResponse.ok(200, "제품 상세 조회 성공",
                new ProductStockResDto(product.getId(), product.getStock()));
    }

    //레디스 재고 감소
    public void decreaseRedisStock(List<UpdateStockReqDto> updateStockReqDtos) {
        redissonLock.updateStockRedisson(updateStockReqDtos);
    }

    // 레디스 재고 증가
    public void increaseRedisStock(List<UpdateStockReqDto> updateStockReqDtos) {
        for (UpdateStockReqDto dto : updateStockReqDtos) {
            redisStockService.incrementStock(dto.getProductId(), dto.getCnt());
        }
    }

    //비관적 락을 사용한 재고 감소
    @Transactional
    public void decreaseDBStock(List<UpdateStockReqDto> updateStockReqDtos) {

        for (UpdateStockReqDto dto : updateStockReqDtos) {

            Product product = productRepository.findByIdWithPessimisticLock(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. productId=" + dto.getProductId()));

            // 재고 부족 처리
            if (product.getStock() < dto.getCnt()) {
                throw new RuntimeException("재고 부족, productId=" + dto.getProductId());
            }

            product.decreaseStock(dto.getCnt());
            productRepository.saveAndFlush(product);
        }
    }

    //재고 증가
    @Transactional
    public void increaseDBStock(List<UpdateStockReqDto> updateStockReqDtos) {

        for (UpdateStockReqDto dto : updateStockReqDtos) {

            // 상품 조회
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. productId=" + dto.getProductId()));

            // 재고 증가
            product.increaseStock(dto.getCnt());
            productRepository.saveAndFlush(product);
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

