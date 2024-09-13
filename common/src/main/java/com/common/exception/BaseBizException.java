package com.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseBizException extends RuntimeException {

    private final HttpStatus status;

    public BaseBizException(String errorMessage) {
        super(errorMessage);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BaseBizException(String errorMessage, HttpStatus status) {
        super(errorMessage);
        this.status = status;
    }
}
