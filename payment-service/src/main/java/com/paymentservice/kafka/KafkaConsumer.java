package com.paymentservice.kafka;

import com.common.dto.payment.PaymentReqDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "payment-request-topic", groupId = "payment-group")
    public void listenPaymentRequest(String payload) throws JsonProcessingException {

        log.info("Received payment request: {}", payload);

        PaymentReqDto paymentReqDto = objectMapper.readValue(payload, PaymentReqDto.class);
        paymentService.processPayment(paymentReqDto);
    }
}
