package com.orderservice.dto.orderHistory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orderservice.dto.order.OrderItemDto;
import com.orderservice.dto.order.OrderResDto;
import com.orderservice.entity.DeliveryStatus;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.entity.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderListDto {

    private Long orderId;
    private LocalDateTime orderDate;
    private int totalPrice;
    private OrderStatus orderStatus;
    private DeliveryStatus deliveryStatus;

    public static OrderListDto from(Order order) {
        return OrderListDto.builder()
                .orderId(order.getId())
                .orderDate(order.getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus())
                .deliveryStatus(order.getDeliveryStatus())
                .build();
    }
}
