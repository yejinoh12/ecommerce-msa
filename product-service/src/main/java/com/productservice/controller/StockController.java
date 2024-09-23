package com.productservice.controller;

import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.product.StockResDto;
import com.productservice.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/product")
@RestController
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    //상품 재고 조회
    @GetMapping("/stock/{productId}")
    public StockResDto getProductStock(@PathVariable("productId") Long productId){
        return stockService.getProductStock(productId);
    }

    // DB 재고 감소
    @PostMapping("/stock/decrease")
    public ResponseEntity<Void> decreaseStocks(@RequestBody UpdateStockReqDto updateStockReqDto) {
        stockService.decreaseDBStock(updateStockReqDto);
        return ResponseEntity.ok().build();
    }

    // DB 재고 증가
    @PostMapping("/stock/increase")
    public ResponseEntity<Void> increaseStocks(@RequestBody UpdateStockReqDto updateStockReqDto) {
        stockService.increaseDBStock(updateStockReqDto);
        return ResponseEntity.ok().build();
    }
}
