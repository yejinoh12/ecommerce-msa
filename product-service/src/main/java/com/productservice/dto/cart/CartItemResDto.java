package com.productservice.dto.cart;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemResDto {
    private Long cartItemId;
    private Long productId;
    private String name;
    private Integer unitPrice;
    private Integer quantity;
    private Integer subTotal;
    private Boolean hasStock;
    private Boolean isInSaleTime;
}

