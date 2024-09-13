package com.common.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReqDto implements Serializable {
    private Long orderId;
    private Long userId;
    private int amount;
}
