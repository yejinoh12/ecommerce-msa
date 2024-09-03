package com.productservice.dto.product;

import com.productservice.domain.Product;
import lombok.*;

@Data
@Builder
public class ProductListDto {
    private Long p_id;
    private String p_name;
    private int price;

    public static ProductListDto from(Product product) {
        return ProductListDto.builder()
                .p_id(product.getId())
                .p_name(product.getProductName())
                .price(product.getPrice())
                .build();
    }
}
