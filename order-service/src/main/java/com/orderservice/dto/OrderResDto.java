package com.orderservice.dto;

import com.orderservice.entity.Order;
import com.orderservice.entity.statusEnum.DeliveryStatus;
import com.orderservice.entity.statusEnum.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResDto {

    private Long orderId;
    private Long userId;
    private LocalDateTime orderDate;
    private int totalPrice;
    private OrderStatus orderStatus;
    private DeliveryStatus deliveryStatus;

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
