package com.common.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderReqDto {

    private Long p_id;
    private int cnt;
    private int subtotal;

}
