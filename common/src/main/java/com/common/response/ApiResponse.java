package com.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Null 값인 필드 제외
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;

    @Builder
    public static <T> ApiResponse<T> ok(int status, String message,  T data) {
        return new ApiResponse<>(status, message, data);
    }

}
