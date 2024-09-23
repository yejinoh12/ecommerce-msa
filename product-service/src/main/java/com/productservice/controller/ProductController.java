package com.productservice.controller;

import com.common.dto.order.AvailCheckReqDto;
import com.common.dto.order.AvailCheckResDto;
import com.common.dto.product.ProductInfoDto;
import com.common.utils.ParseRequestUtil;
import com.productservice.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    //모든 상품 목록 조회
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    //구매 가능 상품 목록 조회
    @GetMapping("/avail")
    public ResponseEntity<?> getAvailProducts() {
        return ResponseEntity.ok(productService.getAvailProducts());
    }

    //상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductDetails(@PathVariable("productId") Long productId){
        return ResponseEntity.status(HttpStatus.OK).body(productService.getProductDetails(productId));
    }

    //좋아요
    @GetMapping("/like/{productId}")
    public ResponseEntity<?> getProductInfos(HttpServletRequest request,
                                             @PathVariable("productId") Long productId) {
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(productService.toggleLikeProduct(userId, productId));
    }

    //주문 서비스에서 상품 구매 가능 여부 확인
    @PostMapping("/purchase/validate")
    public AvailCheckResDto checkPurchaseAvailability(@RequestBody AvailCheckReqDto availCheckReqDto){
        return productService.validatePurchase(availCheckReqDto);
    }

}
