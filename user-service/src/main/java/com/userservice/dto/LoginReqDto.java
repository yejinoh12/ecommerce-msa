package com.userservice.dto;

import lombok.Data;

@Data
public class LoginReqDto {
    private String email;
    private String password;
}