package com.productservice.client;

import com.common.dto.order.UpdateStockReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "order-service", url = "http://localhost:8000")
public interface OrderServiceClient {

    @GetMapping("/order/get/items/{orderId}")
    List<UpdateStockReqDto> getOrderItems(@PathVariable("orderId") Long orderId);

}
