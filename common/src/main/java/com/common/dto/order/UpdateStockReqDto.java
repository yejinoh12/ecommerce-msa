package com.common.dto.order;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStockReqDto implements Serializable {
    private Long productId;
    private int cnt;
}
