package com.orderservice.dto.order;

import com.orderservice.entity.OrderItem;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemDto {

    private String name;
    private int quantity;
    private int price;

    public static OrderItemDto from(OrderItem orderItem) {
        return OrderItemDto.builder()
                .name(orderItem.getProductName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getUnitPrice() * orderItem.getQuantity())
                .build();
    }
}
