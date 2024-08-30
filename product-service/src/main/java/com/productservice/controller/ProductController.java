package com.productservice.controller;

import com.common.dto.product.ProductInfoDto;
import com.productservice.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService stockService;

    //타입별 상품 조회
    @GetMapping
    public ResponseEntity<?> getEventProducts(@RequestParam("type") String type) {
        return ResponseEntity.status(HttpStatus.OK).body(stockService.getProductsList(type));
    }

    //상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductDetailsWithOption( @PathVariable("productId") Long productId){
        return ResponseEntity.status(HttpStatus.OK).body(stockService.getProductDetailsWithOption(productId));
    }

    //상품 정보
    @GetMapping("/info/{productOptionId}")
    public List<ProductInfoDto> getProductInfos(@PathVariable("productOptionId") List<Long> productOptionIds) {
        return stockService.getProductInfos(productOptionIds);
    }

}
