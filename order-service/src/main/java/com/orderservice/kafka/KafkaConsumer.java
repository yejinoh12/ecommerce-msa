package com.orderservice.kafka;

import com.common.dto.order.UpdateStockReqDto;
import com.common.dto.payment.PaymentResDto;
import com.common.exception.BaseBizException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.entity.OrderStatus;
import com.orderservice.repository.OrderItemRepository;
import com.orderservice.repository.OrderRepository;
import com.orderservice.service.StockCacheService;
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
    private final StockCacheService stockCacheService;
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

            //상태 변경
            order.setOrderStatus(OrderStatus.ORDERED);

            //DB 재고 감소 요청
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            List<UpdateStockReqDto> updateStockReqDtos = orderItems.stream()
                    .map(orderItem -> new UpdateStockReqDto(orderItem.getProductId(), orderItem.getQuantity()))
                    .toList();

            kafkaProducer.sendStockUpdateRequest(updateStockReqDtos);

        } else {

            //상태 변경
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);

            //레디스 재고 복구
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            for (OrderItem orderItem : orderItems) {
                log.info("상품 {}의 재고를 복구합니다. 복구 수량: {}", orderItem.getProductId(), orderItem.getQuantity());
                stockCacheService.increaseStock(orderItem.getProductId(), orderItem.getQuantity());
            }
        }

        orderRepository.save(order);
        log.info("주문 상태 변경 orderId={}, orderStatus={}", orderId, order.getOrderStatus());
    }
}
