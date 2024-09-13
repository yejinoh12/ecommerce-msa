package com.common.exception;

import lombok.Getter;

import java.util.Collection;
import java.util.Map;

@Getter
public class FeignClientException extends RuntimeException{

    private final int status;
    private final String errorMessage;

    private final Map<String, Collection<String>> headers;
    private final CustomFeignErrorForm errorForm;

    public FeignClientException(Integer status, String errorMessage,
                                Map<String, Collection<String>> headers, CustomFeignErrorForm errorForm) {
        super(errorMessage);
        this.status = status;
        this.errorMessage = errorMessage;
        this.headers = headers;
        this.errorForm = errorForm;
    }
}