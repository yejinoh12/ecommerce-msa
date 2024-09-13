package com.productservice.controller;

import com.common.dto.order.UpdateStockReqDto;
import com.productservice.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/product")
@RestController
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    //상품 재고 조회
    @GetMapping("/stock/{productId}")
    public ResponseEntity<?> getProductStock(@PathVariable("productId") Long productId){
        return ResponseEntity.status(HttpStatus.OK).body(stockService.getProductStock(productId));
    }

    // Redis 재고 감소
    @PostMapping("/redis/stock/dec")
    public ResponseEntity<Void> decreaseRedisStock(@RequestBody List<UpdateStockReqDto> updateStockReqDtos) {
        stockService.decreaseRedisStock(updateStockReqDtos);
        return ResponseEntity.ok().build();
    }

    // Redis 재고 증가
    @PostMapping("/redis/stock/inc")
    public ResponseEntity<Void> increaseRedisStock(@RequestBody List<UpdateStockReqDto> updateStockReqDtos) {
        stockService.increaseRedisStock(updateStockReqDtos);
        return ResponseEntity.ok().build();
    }

    // DB 재고 감소
    @PostMapping("/db/stock/dec")
    public ResponseEntity<Void> decreaseDBStock(@RequestBody List<UpdateStockReqDto> updateStockReqDtos) {
        stockService.decreaseDBStock(updateStockReqDtos);
        return ResponseEntity.ok().build();
    }

    // DB 재고 증가
    @PostMapping("/db/stock/inc")
    public ResponseEntity<Void> increaseDBStock(@RequestBody List<UpdateStockReqDto> updateStockReqDtos) {
        stockService.increaseDBStock(updateStockReqDtos);
        return ResponseEntity.ok().build();
    }
}
