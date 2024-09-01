package com.productservice.service.product;


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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 목록 조회 (일반 상품/이벤트 상품)
    public ApiResponse<List<ProductListDto>> getProductsList(String type) {

        List<Product> products = fetchProductsByType(type);

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
                .type(getDtype(product))
                .eventStartTime(getEventStartTime(product))
                .eventEndTime(getEventEndTime(product))
                .build();

        return ApiResponse.ok(200, "제품 상세 조회 성공", productDetailsDto);
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

    /**********************************************************
     * 보조 메서드
     **********************************************************/

    private List<Product> fetchProductsByType(String type) {
        return switch (type) {
            case "regular" -> productRepository.findRegularProducts();
            case "event" -> productRepository.findEventProducts();
            default -> throw new BaseBizException("유효하지 않은 상품 타입입니다.");
        };
    }

    public Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BaseBizException("상품을 찾을 수 없습니다. ID: " + productId));
    }

    private String getDtype(Product product) {
        if (product instanceof RegularProduct) {
            return "REGULAR";
        } else if (product instanceof EventProduct) {
            return "EVENT";
        } else {
            throw new BaseBizException("알 수 없는 상품 유형입니다.");
        }
    }

    //상품 상세 조회 시 : 이벤트 상품일 경우 시작 시간을 포함, null 인 경우 json 에 포함 되지 않음
    private LocalDateTime getEventStartTime(Product product) {
        if (product instanceof EventProduct) {
            return ((EventProduct) product).getEventStartTime();
        }
        return null;
    }

    //상품 상세 조회 시 : 이벤트 상품일 경우 종료 시간을 포함, null 인 경우 json 에 포함 되지 않음
    private LocalDateTime getEventEndTime(Product product) {
        if (product instanceof EventProduct) {
            return ((EventProduct) product).getEventEndTime();
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrease(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다. productId=" + productId));
        product.decreaseStock(quantity);
        productRepository.saveAndFlush(product);
    }
}
