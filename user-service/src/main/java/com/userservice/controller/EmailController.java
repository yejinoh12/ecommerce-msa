package com.userservice.controller;

import com.userservice.dto.EmailValidReqDto;
import com.userservice.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    // 이메일 인증 번호 발송
    @PostMapping("/send-code/{email}")
    public ResponseEntity<?> sendEmailVerificationCode(@PathVariable("email") String email) {
        return ResponseEntity.ok(emailService.joinEmail(email));
    }

    // 이메일 인증 번호 검사
    @PostMapping("/verify-code")
    public ResponseEntity<?> confirmEmailVerificationCode(@RequestBody EmailValidReqDto emailValidReqDto) {
        return ResponseEntity.ok(emailService.verifyCode(emailValidReqDto));
    }
}
