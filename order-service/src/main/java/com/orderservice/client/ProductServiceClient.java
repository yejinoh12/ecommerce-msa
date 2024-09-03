package com.orderservice.client;

import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.product.CartResDto;
import com.common.dto.product.ProductInfoDto;
import com.orderservice.config.FeignConfiguration;
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

    //재고 변경
    @PostMapping("/product/stock/update/{action}")
    void updateStock(@RequestBody List<UpdateStockReqDto> updateStockReqDtos, @PathVariable("action") String action);
}