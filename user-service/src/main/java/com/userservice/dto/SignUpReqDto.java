package com.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignUpReqDto {

    @NotBlank(message="이름은 필수 입력값 입니다")
    private String name;

    @NotBlank(message="이메일은 필수 입력값 입니다")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$", message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @NotEmpty(message = "비밀번호 입력은 필수 입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$", message = "비밀번호는 8 ~ 16자 영문 대소문자, 숫자, 특수문자를 사용하세요.")
    private String password;

    @NotEmpty
    private String phoneNumber;

    @NotEmpty
    private String address;

}
