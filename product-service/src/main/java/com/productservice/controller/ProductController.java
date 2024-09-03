package com.productservice.controller;

import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.product.ProductInfoDto;
import com.common.dto.product.StockResponse;
import com.productservice.service.ProductService;
import com.productservice.redis.RedissonLock;
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

    //타입별 상품 조회
    @GetMapping
    public ResponseEntity<?> getEventProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getProductsList());
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

    //상품 재고 조회
    @GetMapping("/stock/{productId}")
    public ResponseEntity<StockResponse> getProductStock(@PathVariable("productId") Long productId){
        return ResponseEntity.status(HttpStatus.OK).body(stockService.getProductStock(productId));
    }

    //주문 서비스에서 재고 변경 요청
    @PostMapping("/stock/update/{action}")
    public ResponseEntity<Void> updateRedisStock(@RequestBody List<UpdateStockReqDto> decreaseStockReqDtos,
                                                 @PathVariable("action") String action) {
        redissonLock.updateStockRedisson(decreaseStockReqDtos, action);
        return ResponseEntity.ok().build();
    }
}
