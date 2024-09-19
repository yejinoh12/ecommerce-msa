package com.productservice.service;

import com.common.dto.order.AvailCheckReqDto;
import com.common.dto.order.AvailCheckResDto;
import com.common.dto.product.ProductInfoDto;
import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.productservice.dto.product.LIkeResDto;
import com.productservice.entity.Like;
import com.productservice.entity.Product;
import com.productservice.dto.product.ProductDetailsResDto;
import com.productservice.dto.product.ProductListResDto;
import com.productservice.repository.LikeRepository;
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
    private final LikeRepository likeRepository;

    // 모든 상품 조회
    public ApiResponse<List<ProductListResDto>> getAllProducts() {

        List<Product> products = productRepository.findAll();
        List<ProductListResDto> productListResDtos = products.stream()
                .map(ProductListResDto::from)
                .collect(Collectors.toList());

        return ApiResponse.ok(200, "제품 목록 조회 성공", productListResDtos);
    }

    // 구매 가능 상품 조회
    public ApiResponse<List<ProductListResDto>> getAvailProducts() {

        // 이벤트 시간 이후 및 재고가 있는 상품 필터링
        List<Product> products = productRepository.findAll().stream()
                .filter(product -> product.isAvailable(LocalDateTime.now()))
                .toList();

        List<ProductListResDto> productListResDtos = products.stream()
                .map(ProductListResDto::from)
                .collect(Collectors.toList());

        return ApiResponse.ok(200, "제품 목록 조회 성공", productListResDtos);
    }

    // 상품 상세 조회
    public ApiResponse<ProductDetailsResDto> getProductDetails(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BaseBizException("상품을 찾을 수 없습니다. ID: " + productId));

        return ApiResponse.ok(200, "제품 상세 조회 성공", ProductDetailsResDto.from(product));
    }

    //좋아요
    public ApiResponse<LIkeResDto> toggleLikeProduct(Long userId, Long productId) {

        //제품 검증
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BaseBizException("상품을 찾을 수 없습니다. ID: " + productId));

        //유저가 해당 제품에 좋아요를 누른 적이 있는지 검증
        Optional<Like> like
                = likeRepository.findByUserIdAndProduct(userId, product);

        if (like.isPresent()) {

            //좋아요 삭제
            likeRepository.delete(like.get());

            //좋아요 업데이트
            product.setLikeCount(product.getLikeCount() - 1);
            productRepository.save(product);

            LIkeResDto dto = new LIkeResDto(productId, product.getLikeCount());

            return ApiResponse.ok(200, "Unlike", dto);

        } else {

            //좋아요 생성 후 저장
            Like newLike = Like.builder()
                    .userId(userId)
                    .product(product)
                    .build();
            likeRepository.save(newLike);

            //좋아요 업데이트
            product.setLikeCount(product.getLikeCount() + 1);
            productRepository.save(product);

            LIkeResDto dto = new LIkeResDto(productId, product.getLikeCount());

            return ApiResponse.ok(200, "Like", dto);

        }
    }

    //주문 서비스에서 상품 정보 요청
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

    //상품 구매 가능 여부 확인
    public AvailCheckResDto validatePurchase(AvailCheckReqDto availCheckReqDto) {

        Long productId = availCheckReqDto.getProductId();
        int quantity = availCheckReqDto.getCount();

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BaseBizException("상품을 찾을 수 없습니다. ID: " + productId));

        // 현재 시간 가져오기
        LocalDateTime currentTime = LocalDateTime.now();

        // 판매 가능 여부와 재고 여부 체크
        boolean hasStock = product.hasEnoughStock(quantity);
        boolean isInSalePeriod = product.isSaleTimeActive(currentTime);

        return AvailCheckResDto.builder()
                .hasStock(hasStock)
                .isInSalePeriod(isInSalePeriod)
                .build();
    }
}
