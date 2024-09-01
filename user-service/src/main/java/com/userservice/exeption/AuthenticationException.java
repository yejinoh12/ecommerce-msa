package com.userservice.exeption;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends org.springframework.security.core.AuthenticationException {

    private final HttpStatus status;

    public AuthenticationException(String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED; // 기본 상태 코드 설정
    }

    public AuthenticationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
