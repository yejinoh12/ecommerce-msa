package com.productservice.service;


import com.common.dto.product.ProductInfoDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.productservice.domain.Product;
import com.productservice.dto.product.ProductDetailsDto;
import com.productservice.dto.product.ProductListDto;
import com.productservice.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    //상품 조회
    public ApiResponse<List<ProductListDto>> getProductsList() {

        List<Product> products = productRepository.findAll();
        List<ProductListDto> productListDtos = products.stream()
                .map(ProductListDto::from)
                .collect(Collectors.toList());

        return ApiResponse.ok(200, "제품 목록 조회 성공", productListDtos);
    }

    // 상품 상세 조회
    public ApiResponse<ProductDetailsDto> getProductDetailsWithOption(Long productId) {

        Product product = findProductById(productId);

        ProductDetailsDto productDetailsDto = ProductDetailsDto.builder()
                .p_id(product.getId())
                .p_name(product.getProductName())
                .price(product.getPrice())
                .eventStartTime(product.getEventStartTime())
                .eventEndTime(product.getEventEndTime())
                .build();

        return ApiResponse.ok(200, "제품 상세 조회 성공", productDetailsDto);
    }

    public Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BaseBizException("상품을 찾을 수 없습니다. ID: " + productId));
    }

    /**********************************************************
     * 주문 서비스 요청 API
     **********************************************************/

    //상품 정보 요청
    public List<ProductInfoDto> getProductInfos(List<Long> productIds) {

        List<Product> products = productRepository.findAllById(productIds);

        if (products.isEmpty()) {
            throw new BaseBizException("해당 상품 옵션 정보를 찾을 수 없습니다.");
        }

        return products.stream()
                .map(productOption -> new ProductInfoDto(
                        productOption.getId(),
                        productOption.getProductName()
                ))
                .collect(Collectors.toList());
    }
}
