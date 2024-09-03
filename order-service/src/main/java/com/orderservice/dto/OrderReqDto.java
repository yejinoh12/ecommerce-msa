package com.orderservice.dto;

import com.common.dto.product.CartResDto;
import lombok.Data;
import java.util.List;

@Data
public class OrderReqDto {
    private Long userId;
    private int totalPrice;
    private List<CartResDto> cartResDtos;
}
