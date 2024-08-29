package com.common.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DecreaseStockReqDto {
    private Long productId;
    private int cnt;
}
