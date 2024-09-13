package com.userservice.exeption;

import com.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class UserServiceExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e) {

        // 유효성 검증 오류 메시지 구성
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ApiResponse<Object> responseMessage = ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("회원 가입 실패")
                .data(errors)
                .build();

        return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
    }
}
