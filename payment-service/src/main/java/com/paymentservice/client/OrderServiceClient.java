package com.paymentservice.client;

import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.payment.PaymentReqDto;
import com.common.dto.payment.PaymentResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "order-service", url = "http://localhost:8000")
public interface OrderServiceClient {

    @GetMapping("/order/get/items/{orderId}")
    List<UpdateStockReqDto> updateRequest(@PathVariable("orderId") Long orderId);

}
