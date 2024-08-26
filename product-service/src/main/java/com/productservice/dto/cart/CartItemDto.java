package com.productservice.dto.cart;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class CartItemDto {

    private Long c_item_id;
    private Long p_id;
    private String p_name;
    private Map<Long, String> opt;
    private int price;
    private int cnt;

}

