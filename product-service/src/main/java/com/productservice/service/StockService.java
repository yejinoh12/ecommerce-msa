package com.productservice.service;

import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.product.StockResDto;
import com.common.exception.BaseBizException;
import com.productservice.entity.Product;
import com.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;

    //재고 조회
    public StockResDto getProductStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BaseBizException("상품을 찾을 수 없습니다. ID: " + productId));
        return new StockResDto(product.getId(), product.getStock());
    }

    //여러 상품에 대한 재고 감소
    @Transactional
    public void decreaseDBStock(List<UpdateStockReqDto> updateStockReqDtos) {

        for (UpdateStockReqDto dto : updateStockReqDtos) {

            Product product = productRepository.findByIdWithPessimisticLock(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. productId=" + dto.getProductId()));

            product.decreaseStock(dto.getCnt());
            productRepository.saveAndFlush(product);
        }
    }

    //여러 상품에 대한 재고 증가
    @Transactional
    public void increaseDBStock(List<UpdateStockReqDto> updateStockReqDtos) {

        for (UpdateStockReqDto dto : updateStockReqDtos) {

            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. productId=" + dto.getProductId()));

            product.increaseStock(dto.getCnt());
            productRepository.saveAndFlush(product);
        }
    }

    //단일 상품에 대한 재고 감소
    @Transactional
    public void decrease(Long productId, int quantity) {
        log.info("db 재고 감소, productId = {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. productId=" + productId));
        product.decreaseStock(quantity);
        productRepository.saveAndFlush(product);
    }

    //단일 상품에 대한 재고 증가
    @Transactional
    public void increase(Long productId, int quantity) {
        log.info("db 재고 증가, productId = {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. productId=" + productId));
        product.increaseStock(quantity);
        productRepository.saveAndFlush(product);
    }
}

