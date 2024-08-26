package com.common.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderReqDto {

    private Long productOptionId;
    private int quantity;
    private int subtotal;

}
