package com.productservice.dto.product;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProductGroupListDto {

    private Long pg_id;
    private String pg_name;
    private List<Long> p_id;
    private int price;

}
