package com.orderservice.dto.order;

import com.common.dto.product.CartItemsDto;
import com.common.dto.user.AddressResDto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OrderPreviewDto {
    private int totalPrice;
    private AddressResDto addressResDto;
    private List<CartItemsDto> orderReqItems;
}
