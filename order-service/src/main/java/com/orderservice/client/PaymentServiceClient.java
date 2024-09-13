package com.orderservice.client;

import com.common.dto.payment.PaymentReqDto;
import com.common.dto.payment.PaymentResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "http://localhost:8000")
public interface PaymentServiceClient {

    @PostMapping("/payment/process")
    PaymentResDto processPayment(@RequestBody PaymentReqDto paymentRequest);
}
