package com.productservice.dto.product;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProductListDto {

    private Long p_id;
    private String p_name;
    private int price;

}
