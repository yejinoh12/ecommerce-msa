package com.productservice.controller;

import com.common.dto.order.PurchaseAvailReqDto;
import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.product.ProductInfoDto;
import com.productservice.redis.RedissonLock;
import com.productservice.service.ProductService;
import com.productservice.service.StockService;
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
    private final StockService stockService;
    private final RedissonLock redissonLock;

    //모든 상품 목록 조회
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getAllProducts());
    }

    //구매 가능 상품 목록 조회
    @GetMapping("/avail")
    public ResponseEntity<?> getAvailProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getAvailProducts());
    }

    //상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductDetailsWithOption( @PathVariable("productId") Long productId){
        return ResponseEntity.status(HttpStatus.OK).body(productService.getProductDetailsWithOption(productId));
    }

    //상품 정보
    @GetMapping("/info/{productId}")
    public List<ProductInfoDto> getProductInfos(@PathVariable("productId") List<Long> productIds) {
        return productService.getProductInfos(productIds);
    }

    //구매 가증 여부 검증
    @PostMapping("/avail")
    public ResponseEntity<Void> validateProductStockAndEventTimes(@RequestBody List<PurchaseAvailReqDto> purchaseAvailReqDtos) {
        productService.checkPurchaseAvailability(purchaseAvailReqDtos);
        return ResponseEntity.ok().build();
    }
}
