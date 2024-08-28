package com.orderservice.dto;

import com.common.dto.product.ProductInfoDto;
import com.orderservice.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private String name;
    private String opt;
    private int cnt;
    private int price;

    public static OrderItemDto from(ProductInfoDto productInfoDto, OrderItem orderItem) {
        return OrderItemDto.builder()
                .name(productInfoDto.getName())
                .opt(productInfoDto.getOpt())
                .cnt(orderItem.getQuantity())
                .price(orderItem.getUnitPrice() * orderItem.getQuantity())
                .build();
    }

}
