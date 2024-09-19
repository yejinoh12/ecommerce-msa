package com.productservice.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LIkeResDto {
    private Long productId;
    private int likeCount;
}
