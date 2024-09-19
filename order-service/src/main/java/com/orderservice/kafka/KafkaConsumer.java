package com.orderservice.kafka;

import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.payment.PaymentResDto;
import com.common.exception.BaseBizException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.dto.order.OrderItemDto;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.entity.OrderStatus;
import com.orderservice.repository.OrderItemRepository;
import com.orderservice.repository.OrderRepository;
import com.orderservice.service.RedisStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final OrderRepository orderRepository;
    private final RedisStockService redisStockService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "payment-response-topic", groupId = "order-group")
    public void handlePaymentResponse(String payload) throws JsonProcessingException {

        //메시지 역직렬화
        PaymentResDto paymentResDto = objectMapper.readValue(payload, PaymentResDto.class);
        log.info("payment-response-topic 수신, orderId={}", paymentResDto.getOrderId());

        //주문 조회
        Long orderId = paymentResDto.getOrderId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BaseBizException("orderID " + orderId + "인 주문을 찾을 수 없습니다."));

        //주문 상태만 변경
        if (paymentResDto.isSuccess()) {
            order.setOrderStatus(OrderStatus.ORDERED);
        } else {
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
        }

        //저장
        orderRepository.save(order);
    }

    @KafkaListener(topics = "stock-increase-topic", groupId = "product-group")
    public void listenStockIncreaseRequest(String payload) throws JsonProcessingException {

        log.info("kafka message -> {}", payload);

        // 메시지 역직렬화
        List<UpdateStockReqDto> products =
                objectMapper.readValue(payload, new TypeReference<List<UpdateStockReqDto>>() {});

        // 각 주문 아이템에 대해 Redis 재고 복구 처리
        for (UpdateStockReqDto dto : products) {
            redisStockService.increaseStock(dto.getProductId(), dto.getCnt());
        }
    }
}
