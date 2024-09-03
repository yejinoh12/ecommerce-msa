package com.orderservice.dto;

import com.common.dto.product.ProductInfoDto;
import com.orderservice.entity.OrderItem;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemDto {

    private String name;
    private int cnt;
    private int price;

    public static OrderItemDto from(ProductInfoDto productInfoDto, OrderItem orderItem) {
        return OrderItemDto.builder()
                .name(productInfoDto.getName())
                .cnt(orderItem.getQuantity())
                .price(orderItem.getUnitPrice() * orderItem.getQuantity())
                .build();
    }
}
