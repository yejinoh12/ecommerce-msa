package com.common.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AvailCheckResDto {
    private boolean hasStock; // 재고가 있는지 여부
    private boolean isInSalePeriod; // 판매 시간 내에 있는지 여부
}
