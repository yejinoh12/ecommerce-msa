package com.productservice.service;


import com.common.dto.order.DecreaseStockReqDto;
import com.common.dto.order.IncreaseStockReqDto;
import com.common.dto.product.ProductInfoDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.productservice.domain.product.EventProduct;
import com.productservice.domain.product.Product;
import com.productservice.domain.product.RegularProduct;
import com.productservice.dto.product.ProductDetailsDto;
import com.productservice.dto.product.ProductListDto;
import com.productservice.dto.product.ProductStock;
import com.productservice.repository.product.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;


    // 상품 목록 조회 (일반 상품 / 이벤트 상품)
    public ApiResponse<List<ProductListDto>> getProductsList(String type) {

        List<Product> products = null;

        if (type.equals("regular")) {
            products = productRepository.findRegularProducts();
        } else if (type.equals("event")) {
            products = productRepository.findEventProducts();
        }

        List<ProductListDto> productListDtos = products.stream()
                .map(product -> ProductListDto.builder()
                        .p_id(product.getId())
                        .p_name(product.getProductName())
                        .price(product.getPrice())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.ok(200, "제품 목록 조회 성공", productListDtos);
    }


    //상품 상세 조회
    public ApiResponse<ProductDetailsDto> getProductDetailsWithOption(Long productId) {

        //상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BaseBizException("productID가 " + productId + "인 상품을 찾을 수 없습니다."));

        // 시작 시간과 종료 시간을 가져오기 위해서 product가 LimitedProduct 인스턴스인지 확인
        LocalDateTime eventStartTime = null;
        LocalDateTime eventEndTime = null;

        if (product instanceof EventProduct eventProduct) {
            eventStartTime = eventProduct.getEventStartTime();
            eventEndTime = eventProduct.getEventEndTime();
        }

        ProductDetailsDto productDetailsDto = ProductDetailsDto.builder()
                .p_id(product.getId())
                .p_name(product.getProductName())
                .price(product.getPrice())
                .type(getDtype(product))
                .eventStartTime(eventStartTime)
                .eventEndTime(eventEndTime)
                .build();

        return ApiResponse.ok(200, "제품 상세 조회 성공", productDetailsDto);
    }

    //재고 조회
    public ApiResponse<ProductStock> getProductStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        ProductStock productStock = new ProductStock(productId, product.getStock());
        return ApiResponse.ok(200, "제품 재고 조회 성공", productStock);
    }

    //재고 감소
    //@Transactional
    public synchronized void decreaseStock(List<DecreaseStockReqDto> decreaseStockReqDtos) {

        log.info("재고 감소 로직 시작");
        for (DecreaseStockReqDto dto : decreaseStockReqDtos) {
            Product product = productRepository.findByIdForUpdate(dto.getProductId())
                    .orElseThrow(() -> new BaseBizException("productID가 " + dto.getProductId() + "인 상품 옵션을 찾을 수 없습니다."));

            product.decreaseStock(dto.getCnt()); // 변경 감지 -> 자동으로 업데이트
            productRepository.saveAndFlush(product);
        }

        log.info("재고 감소 로직 완료");
    }

    public synchronized void decrease(DecreaseStockReqDto dto) {
        Product product = productRepository.findByIdForUpdate(dto.getProductId()).orElseThrow();
        product.decreaseStock(dto.getCnt()); // 변경 감지 -> 자동으로 업데이트
        productRepository.save(product);
    }

    // 재고 증가
    @Transactional
    public void increaseStock(List<IncreaseStockReqDto> increaseStockReqDtos) {

        log.info("재고 증가 로직 시작");
        for (IncreaseStockReqDto dto : increaseStockReqDtos) {

            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new BaseBizException("productID가 " + dto.getProductId() + "인 상품 옵션을 찾을 수 없습니다."));

            product.increaseStock(dto.getCnt()); // 변경 감지 -> 자동으로 업데이트
        }

        log.info("재고 증가 로직 완료");
    }

    //상품 정보
    public List<ProductInfoDto> getProductInfos(List<Long> productIds) {

        List<Product> products = productRepository.findAllById(productIds);

        if (products.isEmpty()) {
            throw new BaseBizException("상품 옵션 정보를 찾을 수 없습니다.");
        }

        //ProductInfoDto
        return products.stream()
                .map(productOption -> new ProductInfoDto(
                        productOption.getId(),
                        productOption.getProductName()
                ))
                .collect(Collectors.toList());
    }

    //dType 조회
    private String getDtype(Product product) {
        if (product instanceof RegularProduct) {
            return "REGULAR";
        } else if (product instanceof EventProduct) {
            return "EVENT";
        } else {
            throw new BaseBizException("상품 정보를 찾을 수 없습니다.");
        }
    }
}
