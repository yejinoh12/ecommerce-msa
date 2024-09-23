package com.productservice.dto.cart;

import com.common.dto.user.AddressResDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartOrderResDto {
    private List<CartItemResDto> items;
    private AddressResDto address;
}