package com.paymentservice.service;

import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.payment.PaymentReqDto;
import com.common.dto.payment.PaymentResDto;
import com.paymentservice.client.OrderServiceClient;
import com.paymentservice.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final KafkaProducer kafkaProducer;

    public PaymentResDto processPayment(PaymentReqDto paymentReqDto) {

        //결제 결과
        boolean success = true;

        // 결제 결과 전송
        PaymentResDto paymentResDto = new PaymentResDto(paymentReqDto.getOrderId(), success);
        kafkaProducer.sendPaymentResponse(paymentResDto);

        List<UpdateStockReqDto> products = paymentReqDto.getUpdateStockReqDtos();

        //결제 성공 여부에 따라 다른 이벤트 발행
        if (success) {
            kafkaProducer.sendDBStockDecreaseRequest(products);
        } else {
            kafkaProducer.sendRedisStockIncreaseRequest(products);
        }

        return paymentResDto;
    }
}
