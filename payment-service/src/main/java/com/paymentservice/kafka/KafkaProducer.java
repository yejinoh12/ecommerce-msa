package com.paymentservice.kafka;

import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.payment.PaymentReqDto;
import com.common.dto.payment.PaymentResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    //결제 완료에 대한 응답
    public void sendPaymentResponse(PaymentResDto paymentResDto) {
        kafkaTemplate.send("payment-response-topic", paymentResDto);
    }

    //결제 성공 시 재고 감소 요청
    public void sendDBStockDecreaseRequest(List<UpdateStockReqDto> products) {
        kafkaTemplate.send("stock-decrease-topic", products);
    }

    //결제 실패시 레디스 재고 증가 요청
    public void sendRedisStockIncreaseRequest(List<UpdateStockReqDto> products) {
        kafkaTemplate.send("stock-increase-topic", products);
    }

}