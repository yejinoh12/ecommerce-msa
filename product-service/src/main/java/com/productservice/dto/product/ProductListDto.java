package com.productservice.dto.product;

import com.productservice.domain.Product;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductListDto implements Serializable {

    private Long p_id;
    private String p_name;
    private int price;
    private boolean hasStock;
    private boolean isInSaleTime;

    public static ProductListDto from(Product product) {
        return ProductListDto.builder()
                .p_id(product.getId())
                .p_name(product.getName())
                .price(product.getPrice())
                .hasStock(product.hasStock())
                .isInSaleTime(product.isSaleTimeActive(LocalDateTime.now()))
                .build();
    }
}
