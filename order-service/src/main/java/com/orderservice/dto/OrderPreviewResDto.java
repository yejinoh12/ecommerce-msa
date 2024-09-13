package com.orderservice.dto;

import com.common.dto.product.CartResDto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OrderPreviewResDto {
    private int totalPrice;
    private List<CartResDto> orderReqItems;
}
