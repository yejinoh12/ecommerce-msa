package com.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orderservice.entity.Order;
import com.orderservice.entity.DeliveryStatus;
import com.orderservice.entity.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResDto {

    private Long orderId;
    private Long userId;
    private LocalDateTime orderDate;
    private int totalPrice;
    private OrderStatus orderStatus;
    private DeliveryStatus deliveryStatus;

    public static OrderResDto from(Order order) {
        return OrderResDto.builder()
                .orderId(order.getId())
                .orderDate(order.getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .build();
    }

    public static OrderResDto from(Order order, Long userId) {
        return OrderResDto.builder()
                .orderId(order.getId())
                .userId(userId)
                .orderDate(order.getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus())
                .deliveryStatus(order.getDeliveryStatus())
                .build();
    }
}
