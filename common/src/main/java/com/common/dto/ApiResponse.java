package com.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Null 값인 필드 제외
public class ApiResponse<T> {

    private int statusCode;
    private String resultMessage;
    private T data;

    public static <T> ApiResponse<T> ok(int statusCode, String message, T data) {
        return new ApiResponse<>(statusCode, message, data);
    }

}
