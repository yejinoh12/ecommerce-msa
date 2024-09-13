package com.productservice.dto.product;

import com.productservice.domain.Product;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductDetailDto {

    private Long p_id;
    private String name;
    private String description;
    private int price;
    private int stock;
    private LocalDateTime startFrom;
    private boolean hasStock;
    private boolean isInSaleTime;

    public static ProductDetailDto from(Product product) {
        return ProductDetailDto.builder()
                .p_id(product.getId())
                .description(product.getDescription())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .startFrom(product.getStartTime())
                .hasStock(product.hasStock())
                .isInSaleTime(product.isAvailable(LocalDateTime.now()))
                .build();
    }
}
