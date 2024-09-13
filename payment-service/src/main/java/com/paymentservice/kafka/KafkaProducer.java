package com.paymentservice.kafka;

import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.payment.PaymentResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentResponse(PaymentResDto paymentResDto) {
        kafkaTemplate.send("payment-response-topic", paymentResDto);
    }
}