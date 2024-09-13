package com.common.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResDto implements Serializable {
    private Long orderId; // 결제 요청에 대한 주문 ID
    private boolean success; // 결제 성공 여부
}
