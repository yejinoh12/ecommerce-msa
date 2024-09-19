package com.productservice.dto.product;

import com.productservice.entity.Product;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductDetailsResDto {

    private Long p_id;
    private String name;
    private String description;
    private int likeCount;
    private int price;
    private int stock;
    private LocalDateTime startFrom;
    private boolean hasStock;
    private boolean isInSaleTime;

    public static ProductDetailsResDto from(Product product) {
        return ProductDetailsResDto.builder()
                .p_id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .likeCount(product.getLikeCount())
                .price(product.getPrice())
                .stock(product.getStock())
                .startFrom(product.getStartTime())
                .hasStock(product.hasStock())
                .isInSaleTime(product.isSaleTimeActive(LocalDateTime.now()))
                .build();
    }
}
