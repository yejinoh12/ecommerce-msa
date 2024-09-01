package com.userservice.dto;

import lombok.Data;

@Data
public class EmailValidReqDto {
    private String email;
    private String code;
}
