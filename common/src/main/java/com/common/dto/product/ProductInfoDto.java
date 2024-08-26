package com.common.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductInfoDto {

    private Long productOptionId;
    private String name;    //상품 풀네임(그룹네임+태그)
    private String opt;     //옵션

}
