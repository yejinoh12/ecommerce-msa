package com.productservice.controller;

import com.common.utils.ParseRequestUtil;
import com.productservice.dto.cart.CartAddDto;
import com.productservice.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    //상품 목록 조회
    @GetMapping
    public ResponseEntity<?> getProductsByCategory() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getProductsList());
    }

    //상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductDetailsWithOption( @PathVariable("productId") Long productId){
        return ResponseEntity.status(HttpStatus.OK).body(productService.getProductDetailsWithOption(productId));
    }

}
