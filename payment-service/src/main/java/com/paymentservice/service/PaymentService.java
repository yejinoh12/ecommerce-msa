package com.paymentservice.service;

import com.common.dto.payment.PaymentReqDto;
import com.common.dto.payment.PaymentResDto;
import com.paymentservice.PaymentStatus;
import com.paymentservice.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final KafkaProducer kafkaProducer;

    public PaymentResDto processPayment(PaymentReqDto paymentReqDto) {

        boolean success = processPaymentLogic(paymentReqDto);

        // 결제 결과 전송
        PaymentResDto paymentResDto = new PaymentResDto(paymentReqDto.getOrderId(), success);
        kafkaProducer.sendPaymentResponse(paymentResDto);
        return paymentResDto;
    }

    private boolean processPaymentLogic(PaymentReqDto paymentRequest) {
        return true;
    }
}
