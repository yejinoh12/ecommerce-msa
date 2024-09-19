package com.orderservice.kafka;

import com.common.dto.payment.PaymentReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentRequest(PaymentReqDto paymentReqDto) {
        log.info("Kafka 주문 요청 orderId={}", paymentReqDto.getOrderId());
        kafkaTemplate.send("payment-request-topic", paymentReqDto);
    }
}
