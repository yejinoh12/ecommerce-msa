package com.productservice.dto.product;

import com.productservice.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductListResDto implements Serializable {

    private Long p_id;
    private String p_name;
    private int price;
    private boolean hasStock;
    private boolean isInSaleTime;

    public static ProductListResDto from(Product product) {
        return ProductListResDto.builder()
                .p_id(product.getId())
                .p_name(product.getName())
                .price(product.getPrice())
                .hasStock(product.hasStock())
                .isInSaleTime(product.isSaleTimeActive(LocalDateTime.now()))
                .build();
    }
}
