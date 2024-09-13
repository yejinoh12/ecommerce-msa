package com.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomFeignErrorForm {

    private String status;
    private String message;

    public CustomFeignErrorForm(String status, String message) {
        this.status = status;
        this.message = message;
    }
}