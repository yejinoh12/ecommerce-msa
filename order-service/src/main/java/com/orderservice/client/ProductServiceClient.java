package com.orderservice.client;

import com.common.dto.order.AvailCheckReqDto;
import com.common.dto.order.AvailCheckResDto;
import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.product.CartResDto;
import com.common.dto.product.ProductInfoDto;
import com.common.dto.product.StockResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service", url = "http://localhost:8000")
public interface ProductServiceClient {

    //장바구니 조회
    @GetMapping("/cart/get-items")
    List<CartResDto> getOrderItems(@RequestHeader("X-Claim-userId") Long userId);

    //장바구니 삭제
    @GetMapping("/cart/clear/after-order")
    void deleteCartAfterOrder(@RequestHeader("X-Claim-userId") Long userId);

    //상품 조회
    @GetMapping("/product/info/{productId}")
    List<ProductInfoDto> getProductInfos(@PathVariable("productId") List<Long> productIds);

    //DB 재고 감소
    @PostMapping("/product/stock/decrese")
    void decreaseDBStock(@RequestBody UpdateStockReqDto updateStockReqDto);

    //DB 재고 증가
    @PostMapping("/product/stock/increase")
    void increaseDBStock(@RequestBody UpdateStockReqDto updateStockReqDto);

    //재고 정보 조회
    @GetMapping("/product/stock/{productId}")
    StockResDto getProductStock(@PathVariable("productId") Long productId);

    @PostMapping("/product/purchase/validate")
    AvailCheckResDto checkPurchaseAvailability(@RequestBody AvailCheckReqDto availCheckReqDto);
}