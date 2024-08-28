package com.orderservice.client;

import com.common.dto.order.CreateOrderReqDto;
import com.common.dto.order.DecreaseStockReqDto;
import com.common.dto.order.IncreaseStockReqDto;
import com.common.dto.product.ProductInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service", url = "http://localhost:8000")
public interface ProductServiceClient {

    //장바구니 조회
    @GetMapping("/cart/{userId}/orders")
    List<CreateOrderReqDto> getOrderItems(@PathVariable("userId") Long userId);

    // 재고 증가
    @PostMapping("/product/option-stock/increase")
    void increaseStock(List<IncreaseStockReqDto> increaseStockRequestDtos);

    // 재고 감소
    @PostMapping("/product/option-stock/decrease")
    void decreaseStock(@RequestBody List<DecreaseStockReqDto> decreaseStockRequestDtos);

    //상품 조회
    @GetMapping("/product/info/{productOptionId}")
    List<ProductInfoDto> getProductInfos(@PathVariable("productOptionId") List<Long> productOptionIds);

}
