package com.productservice.dto.cart;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartOrderResDto {
    private Long productId;
    private String name;
    private int unitPrice;
    private int cnt;
    private String addrAlias;
    private String address;
    private String addrDetail;
    private String phone;
}