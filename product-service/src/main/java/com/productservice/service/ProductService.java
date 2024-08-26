package com.productservice.service;

import com.common.dto.ApiResponse;
import com.common.dto.order.DecreaseStockReqDto;
import com.common.dto.order.IncreaseStockReqDto;
import com.common.dto.product.ProductInfoDto;
import com.productservice.domain.product.Product;
import com.productservice.domain.product.ProductGroup;
import com.productservice.domain.product.ProductOption;
import com.productservice.dto.product.ProductDetailsDto;
import com.productservice.dto.product.ProductGroupListDto;
import com.productservice.repository.product.ProductGroupRepository;
import com.productservice.repository.product.ProductOptionRepository;
import com.productservice.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductGroupRepository productGroupRepository;
    private final ProductOptionRepository productOptionRepository;

    //상품 목록 조회
    public ApiResponse<List<ProductGroupListDto>> getProductsList() {

        //그룹 조회
        List<ProductGroup> productGroups = productGroupRepository.findAll();

        //상품 조회
        List<ProductGroupListDto> productGroupListDtos = productGroups.stream()

                .filter(productGroup -> !productGroup.getProducts().isEmpty()) // Product 가 있는 경우만 필터링
                .map(productGroup -> {

                    List<Product> products = productGroup.getProducts();
                    return ProductGroupListDto.builder()
                            .pg_id(productGroup.getId())
                            .pg_name(productGroup.getGroupName())
                            .p_id(products.stream().map(Product::getId).sorted().toList()) // id 정렬
                            .price(productGroup.getPrice())
                            .build();
                })
                .collect(Collectors.toList());

        return ApiResponse.ok(200, "제품 목록 조회 성공", productGroupListDtos);
    }


    //상품 상세 조회
    public ApiResponse<ProductDetailsDto> getProductDetailsWithOption(Long productId) {

        //상품 조회
        Product product = productRepository.findProductsById(productId)
                .orElseThrow(() -> new NoSuchElementException("등록된 상품이 없습니다."));

        //상품 옵션 조회
        List<ProductOption> getOptions = productOptionRepository.findByProductId(productId);

        //Map (옵션, 재고)
        Map<String, Integer> optionNameAndStock = getOptions.stream()
                .collect(Collectors.toMap(ProductOption::getOptionName, ProductOption::getStock));

        ProductDetailsDto productDetailsDto = ProductDetailsDto.builder()
                .p_id(product.getId())
                .p_name(product.getProductGroup().getGroupName() + " - " + product.getTag())
                .price(product.getProductGroup().getPrice())
                .option(optionNameAndStock)
                .build();

        return ApiResponse.ok(200,"제품 상세 조회 성공", productDetailsDto);
    }

    // 재고 감소
    @Transactional
    public void decreaseStock(List<DecreaseStockReqDto> decreaseStockReqDtos) {


        log.info("재고 감소 로직 시작");
        log.info("decreaseStockReqDto.get(0).getProductOptionId() = {}", decreaseStockReqDtos.get(0).getProductOptionId());

        for (DecreaseStockReqDto dto : decreaseStockReqDtos) {
            log.info("order-service request / 재고 = {}", dto.getProductOptionId().toString());
            ProductOption productOption = productOptionRepository.findById(dto.getProductOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("상품 옵션을 찾을 수 없습니다."));

            productOption.decreaseStock(dto.getQuantity()); // 변경 감지 -> 자동으로 업데이트
        }


        log.info("재고 감소 로직 완료");
    }


    // 재고 증가
    @Transactional
    public void increaseStock(List<IncreaseStockReqDto> increaseStockRequestDtos) {
        for (IncreaseStockReqDto dto : increaseStockRequestDtos) {
            ProductOption productOption = productOptionRepository.findById(dto.getProductOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("상품 옵션을 찾을 수 없습니다."));

            productOption.increaseStock(dto.getQuantity());
        }
    }

    public List<ProductInfoDto> getProductInfos(List<Long> productOptionIds) {

        List<ProductOption> productOptions = productOptionRepository.findWithProductAndGroupById(productOptionIds);

        if (productOptions.isEmpty()) {
            throw new NoSuchElementException("상품 옵션을 찾을 수 없습니다.");
        }

        //ProductInfoDto
        return productOptions.stream()
                .map(productOption -> new ProductInfoDto(
                        productOption.getId(),
                        productOption.getProduct().getProductGroup().getGroupName() + "-" + productOption.getProduct().getTag(),
                        productOption.getOptionName()
                ))
                .collect(Collectors.toList());
    }
}
