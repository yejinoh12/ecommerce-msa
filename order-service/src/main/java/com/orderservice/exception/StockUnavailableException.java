package com.orderservice.exception;

import com.common.exception.BaseBizException;

public class StockUnavailableException extends BaseBizException {

    public StockUnavailableException(String errorMessage) {
        super(errorMessage);
    }
}
