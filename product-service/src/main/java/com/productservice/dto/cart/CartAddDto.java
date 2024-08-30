package com.productservice.dto.cart;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartAddDto {
    private Long p_id;
    private int cnt;

}

