package com.orderservice.exception;

import com.common.exception.BaseBizException;

public class PaymentFailureException extends BaseBizException {

    public PaymentFailureException(String errorMessage) {
        super(errorMessage);
    }
}
