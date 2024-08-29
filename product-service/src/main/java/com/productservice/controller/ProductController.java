package com.productservice.controller;

import com.common.dto.order.DecreaseStockReqDto;
import com.common.dto.order.IncreaseStockReqDto;
import com.common.dto.product.ProductInfoDto;
import com.productservice.service.ProductService;
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

    private final ProductService productService;

    //타입별 상품 조회
    @GetMapping
    public ResponseEntity<?> getEventProducts(@RequestParam("type") String type) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getProductsList(type));
    }

    //상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductDetailsWithOption( @PathVariable("productId") Long productId){
        return ResponseEntity.status(HttpStatus.OK).body(productService.getProductDetailsWithOption(productId));
    }

    //상품 재고 조회
    @GetMapping("/stock/{productId}")
    public ResponseEntity<?> getProductStock( @PathVariable("productId") Long productId){
        return ResponseEntity.status(HttpStatus.OK).body(productService.getProductStock(productId));
    }

    /**
     * 주문 서비스 요청 API
     */

    // 재고 증가
    @PostMapping("/option-stock/increase")
    public ResponseEntity<Void> increaseStock(@RequestBody List<IncreaseStockReqDto> increaseStockRequestDtos) {
        log.info("order-service 재고 증가 요청");
        productService.increaseStock(increaseStockRequestDtos);
        return ResponseEntity.ok().build();
    }

    //재고 감소
    @PostMapping("/option-stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<DecreaseStockReqDto> decreaseStockRequestDtos) {
        productService.decreaseStock(decreaseStockRequestDtos);
        return ResponseEntity.ok().build();
    }

    //상품 정보
    @GetMapping("/info/{productOptionId}")
    public List<ProductInfoDto> getProductInfos(@PathVariable("productOptionId") List<Long> productOptionIds) {
        return productService.getProductInfos(productOptionIds);
    }

}
