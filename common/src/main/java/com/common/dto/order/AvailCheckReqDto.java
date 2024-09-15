package com.common.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AvailCheckReqDto {

    private Long productId;
    private int count;

}
