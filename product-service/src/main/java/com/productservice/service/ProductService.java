package com.productservice.service;

import com.common.dto.order.PurchaseAvailReqDto;
import com.common.dto.product.ProductInfoDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.productservice.domain.Product;
import com.productservice.dto.product.ProductDetailDto;
import com.productservice.dto.product.ProductListDto;
import com.productservice.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 모든 상품 조회
    public ApiResponse<List<ProductListDto>> getAllProducts() {

        List<Product> products = productRepository.findAll();

        List<ProductListDto> productListDtos = products.stream()
                .map(ProductListDto::from)
                .collect(Collectors.toList());

        return ApiResponse.ok(200, "제품 목록 조회 성공", productListDtos);
    }

    // 구매 가능 상품 조회
    public ApiResponse<List<ProductListDto>> getAvailProducts() {

        // 이벤트 시간 이후 및 재고가 있는 상품 필터링
        List<Product> products = productRepository.findAll().stream()
                .filter(product -> product.isAvailable(LocalDateTime.now()))
                .toList();

        List<ProductListDto> productListDtos = products.stream()
                .map(ProductListDto::from)
                .collect(Collectors.toList());

        return ApiResponse.ok(200, "제품 목록 조회 성공", productListDtos);
    }

    // 상품 상세 조회
    public ApiResponse<ProductDetailDto> getProductDetailsWithOption(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BaseBizException("상품을 찾을 수 없습니다. ID: " + productId));

        return ApiResponse.ok(200, "제품 상세 조회 성공", ProductDetailDto.from(product));
    }

    //상품 정보 요청
    public List<ProductInfoDto> getProductInfos(List<Long> productIds) {

        List<Product> products = productRepository.findAllById(productIds);

        if (products.isEmpty()) {
            throw new BaseBizException("해당 상품 옵션 정보를 찾을 수 없습니다.");
        }

        return products.stream()
                .map(product -> ProductInfoDto.builder()
                        .productId(product.getId())
                        .name(product.getName())
                        .build())
                .collect(Collectors.toList());
    }

    // 구매 가능 여부 검증
    public void checkPurchaseAvailability(List<PurchaseAvailReqDto> purchaseAvailReqDtos) {

        LocalDateTime currentTime = LocalDateTime.now();

        for (PurchaseAvailReqDto reqDto : purchaseAvailReqDtos) {

            // 상품 ID로 상품 조회
            Optional<Product> optionalProduct = productRepository.findById(reqDto.getProductId());
            if (optionalProduct.isEmpty()) {
                throw new BaseBizException("상품 ID " + reqDto.getProductId() + "에 해당하는 상품이 없습니다.");
            }

            Product product = optionalProduct.get();

            // 요청된 수량 검증
            if (!product.hasSufficientStock(reqDto.getCount())) {
                throw new BaseBizException("상품 ID " + reqDto.getProductId() + "의 재고가 부족합니다. " +
                        "요청 수량: " + reqDto.getCount() + ", 현재 재고: " + product.getStock());
            }

            // 판매 시간 검증
            if (!product.isSaleTimeActive(currentTime)) {
                throw new BaseBizException("상품 ID " + reqDto.getProductId() + "은(는) 현재 판매 시간이 아닙니다." +
                        product.getStartTime() + " 부터 구매 가능합니다. ");
            }
        }
    }
}
