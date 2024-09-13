package com.common.exception;

import com.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseBizException.class)
    public ResponseEntity<?> handleBaseBizException(BaseBizException e) {

        log.error("Business Error: {}", e.getMessage(), e);

        ApiResponse<Object> responseMessage = ApiResponse.builder()
                .status(e.getStatus().value())
                .message(e.getMessage())
                .build();

        return new ResponseEntity<>(responseMessage, e.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {

        log.error("Unexpected Error: {}", e.getMessage(), e);

        ApiResponse<Object> responseMessage = ApiResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred: " + e.getMessage())
                .build();

        return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FeignClientException.class)
    public ResponseEntity<?> FeignClientExceptionHandler(FeignClientException ex) {

        log.error("feign error status: {}", ex.getStatus());

        ApiResponse<Object> responseMessage = ApiResponse.builder()
                .status(ex.getStatus())
                .message(ex.getErrorForm().getMessage())
                .build();

        return new ResponseEntity<>(responseMessage, HttpStatusCode.valueOf(ex.getStatus()));
    }
}
