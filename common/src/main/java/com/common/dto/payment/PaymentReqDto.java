package com.common.dto.payment;

import com.common.dto.order.UpdateStockReqDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReqDto {
    private Long orderId;
    private int amount;
    private List<UpdateStockReqDto> updateStockReqDtos;
}

