package com.orderservice.client;

import com.common.dto.order.AvailCheckReqDto;
import com.common.dto.order.AvailCheckResDto;
import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.product.StockResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", url = "http://localhost:8000")
public interface ProductServiceClient {

    //장바구니 삭제
    @GetMapping("/cart/clear/after-order")
    void deleteCartAfterOrder(@RequestHeader("X-Claim-userId") Long userId);

    //DB 재고 증가
    @PostMapping("/product/stock/increase")
    void increaseDBStock(@RequestBody UpdateStockReqDto updateStockReqDto);

    //재고 정보 조회
    @GetMapping("/product/stock/{productId}")
    StockResDto getProductStock(@PathVariable("productId") Long productId);

    @PostMapping("/product/purchase/validate")
    AvailCheckResDto checkPurchaseAvailability(@RequestBody AvailCheckReqDto availCheckReqDto);
}