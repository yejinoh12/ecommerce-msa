package com.productservice.dto.product;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class ProductDetailsDto {

    private Long p_id;
    private String p_name;
    private int price;
    private Map<String, Integer> option;

}
