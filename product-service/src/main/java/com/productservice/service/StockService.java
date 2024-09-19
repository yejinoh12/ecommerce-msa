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

    //재고 감소
    @Transactional
    public void decreaseDBStock(UpdateStockReqDto updateStockReqDto) {

        Long productId = updateStockReqDto.getProductId();
        int quantity = updateStockReqDto.getCnt();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. productId=" + productId));

        product.decreaseStock(quantity);
        productRepository.save(product);
    }

    //재고 증가
    @Transactional
    public void increaseDBStock(UpdateStockReqDto updateStockReqDto) {

        Long productId = updateStockReqDto.getProductId();
        int quantity = updateStockReqDto.getCnt();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. productId=" + productId));
        product.increaseStock(quantity);
        productRepository.save(product);
    }
}

