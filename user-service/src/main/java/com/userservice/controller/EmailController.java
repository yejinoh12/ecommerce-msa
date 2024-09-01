package com.userservice.controller;

import com.userservice.dto.EmailValidReqDto;
import com.userservice.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    // 이메일 인증 번호 발송
    @PostMapping("/send-code")
    public ResponseEntity<?> sendEmailVerificationCode(@RequestParam("email") String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emailService.joinEmail(email));
    }

    // 이메일 인증 번호 검사
    @PostMapping("/verify-code")
    public ResponseEntity<?> confirmEmailVerificationCode(@RequestBody EmailValidReqDto emailValidReqDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emailService.verifyCode(emailValidReqDto));
    }
}
