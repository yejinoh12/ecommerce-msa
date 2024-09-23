package com.orderservice.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orderservice.entity.OrderItem;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemDto {

    private Long productId;
    private String name;
    private int quantity;
    private int unitPrice;

    public static OrderItemDto from(OrderItem orderItem) {
        return OrderItemDto.builder()
                .productId(orderItem.getProductId())
                .name(orderItem.getProductName())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .build();
    }
}
