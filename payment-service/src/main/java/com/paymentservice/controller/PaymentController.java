package com.paymentservice.controller;

import com.common.dto.payment.PaymentReqDto;
import com.common.dto.payment.PaymentResDto;
import com.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public PaymentResDto processPayment(@RequestBody PaymentReqDto paymentRequest) {
        return paymentService.processPayment(paymentRequest);
    }
}
