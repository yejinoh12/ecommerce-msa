package com.orderservice.client;

import com.common.dto.order.CreateOrderReqDto;
import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.product.ProductInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service", url = "http://localhost:8000")
public interface ProductServiceClient {

    //장바구니 조회
    @GetMapping("/cart/get-items")
    List<CreateOrderReqDto> getOrderItems(@RequestHeader("X-Claim-userId") Long userId);

    //장바구니 삭제
    @GetMapping("/cart/clear/after-order")
    void deleteCartAfterOrder(@RequestHeader("X-Claim-userId") Long userId);

    //상품 조회
    @GetMapping("/product/info/{productOptionId}")
    List<ProductInfoDto> getProductInfos(@PathVariable("productOptionId") List<Long> productOptionIds);

    //재고 변경
    @PostMapping("product/update-redis-stock")
    void updateStock(@RequestBody List<UpdateStockReqDto> updateStockReqDtos);

    //제품 재고 동기화
    @PostMapping("/product/update-db-stock")
    void synchronizeStock(@RequestBody List<UpdateStockReqDto> syncStockReqDtos);

}