package com.orderservice.exception;

import com.common.exception.BaseBizException;

public class PaymentAbortedException extends BaseBizException {
    public PaymentAbortedException(String errorMessage) {
        super(errorMessage);
    }
}
