package com.orderservice.dto.order;

import com.common.dto.product.CartResDto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OrderPreviewDto {
    private int totalPrice;
    private List<CartResDto> orderReqItems;
}
