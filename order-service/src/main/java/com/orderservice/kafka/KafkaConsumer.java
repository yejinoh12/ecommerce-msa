package com.orderservice.kafka;

import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.payment.PaymentReqDto;
import com.common.dto.payment.PaymentResDto;
import com.common.exception.BaseBizException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.entity.statusEnum.OrderStatus;
import com.orderservice.repository.OrderItemRepository;
import com.orderservice.repository.OrderRepository;
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
    private final OrderItemRepository orderItemRepository;
    private final KafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "payment-response-topic", groupId = "order-group")
    public void handlePaymentResponse(String payload) throws JsonProcessingException {

        PaymentResDto paymentResDto = objectMapper.readValue(payload, PaymentResDto.class);
        log.info("payment-response-topic 수신, orderId={}", paymentResDto.getOrderId());

        Long orderId = paymentResDto.getOrderId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BaseBizException("orderID " + orderId + "인 주문을 찾을 수 없습니다."));

        //주문 상태 변경
        if (paymentResDto.isSuccess()) {
            order.setOrderStatus(OrderStatus.ORDERED);

            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            List<UpdateStockReqDto> updateStockReqDtos = orderItems.stream()
                    .map(orderItem -> new UpdateStockReqDto(orderItem.getProductId(), orderItem.getQuantity()))
                    .toList();

            //재고 감소 요청
            kafkaProducer.sendStockUpdateRequest(updateStockReqDtos);

        } else {
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
        }

        orderRepository.save(order);
        log.info("주문 상태 변경 orderId={}, orderStatus={}", orderId, order.getOrderStatus());
    }
}
