package com.orderservice.client;

import com.common.dto.order.PurchaseAvailReqDto;
import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.product.CartResDto;
import com.common.dto.product.ProductInfoDto;
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

    //구매 가능 여부 확인
    @PostMapping("/product/avail")
    void validateProductStockAndEventTimes(@RequestBody List<PurchaseAvailReqDto> purchaseAvailReqDtos);

    //재고 변경
    @PostMapping("/product/stock/update/{action}")
    void updateRedisStock(@RequestBody List<UpdateStockReqDto> updateStockReqDtos, @PathVariable("action") String action);

    //Redis 재고 감소
    @PostMapping("/product/redis/stock/dec")
    void decreaseRedisStock(@RequestBody List<UpdateStockReqDto> updateStockReqDtos);

    //Redis 재고 증가
    @PostMapping("/product/redis/stock/inc")
    void increaseRedisStock(@RequestBody List<UpdateStockReqDto> updateStockReqDtos);

    //DB 재고 감소
    @PostMapping("/product/db/stock/dec")
    void decreaseDBStock(@RequestBody List<UpdateStockReqDto> updateStockReqDtos);

    //DB 재고 증가
    @PostMapping("/product/db/stock/inc")
    void increaseDBStock(@RequestBody List<UpdateStockReqDto> updateStockReqDtos);
}