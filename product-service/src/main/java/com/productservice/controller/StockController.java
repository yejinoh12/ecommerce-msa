package com.productservice.controller;

import com.common.dto.order.CreateOrderReqDto;
import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.product.StockResponse;
import com.productservice.service.stock.RedisStockLockService;
import com.productservice.service.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final RedisStockLockService redisStockLockService;

    //상품 재고 조회
    @GetMapping("/stock/{productId}")
    public ResponseEntity<StockResponse> getProductStock(@PathVariable("productId") Long productId){
        return ResponseEntity.status(HttpStatus.OK).body(stockService.getProductStock(productId));
    }

    @PostMapping("/stock/sync")
    public ResponseEntity<Void> updateDbStock(@RequestBody List<UpdateStockReqDto> updateStockReqDtos) {
        redisStockLockService.updateStockRedisson(updateStockReqDtos);
        return ResponseEntity.ok().build();
    }

    //미사용
    @PostMapping("/update-redis-stock")
    public ResponseEntity<Void> updateRedisStock(@RequestBody List<UpdateStockReqDto> decreaseStockReqDtos) {
        redisStockLockService.updateStockRedisson(decreaseStockReqDtos);
        return ResponseEntity.ok().build();
    }
}