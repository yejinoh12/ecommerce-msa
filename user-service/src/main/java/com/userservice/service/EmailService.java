package com.userservice.service;

import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.userservice.dto.EmailValidReqDto;
import com.userservice.redis.RedisEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisEmailService redisEmailService;

    @Value("${email.setForm}")
    private String setForm;

    private static final String EMAIL_TITLE = "회원 가입을 위한 인증 이메일";
    private static final String EMAIL_CONTENT_TEMPLATE = "아래의 인증번호를 입력하여 회원가입을 완료해주세요."+
            "<br><br>" +
            "인증번호 %s";

    //전송 및 응답
    public ApiResponse<String> joinEmail(String email) {
        String code = Integer.toString(makeRandomNumber());
        String content = String.format(EMAIL_CONTENT_TEMPLATE, code);
        mailSend(setForm, email, EMAIL_TITLE, content);
        redisEmailService.setEmailVerificationCode(email, code); //Redis 저장
        return ApiResponse.ok(200, "인증 번호 발송 성공", code);
    }

    //메일 전송
    public void mailSend(String setFrom, String toMail, String title, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"utf-8");
            helper.setFrom(setFrom);
            helper.setTo(toMail);
            helper.setSubject(title);
            helper.setText(content,true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error(e.getMessage());
        }
    }

    //번호 생성
    public int makeRandomNumber() {
        Random r = new Random();
        StringBuilder randomNumber = new StringBuilder();
        for(int i = 0; i < 6; i++) {
            randomNumber.append(r.nextInt(10));
        }
        return Integer.parseInt(randomNumber.toString());
    }

    //이메일 인증 완료 후 상태 변경
    public ApiResponse<?> verifyCode(EmailValidReqDto emailValidReqDto) {

        String code = redisEmailService.getEmailVerificationCode(emailValidReqDto.getEmail());

        if(code == null) {
            throw new BaseBizException("등록되지 않은 이메일입니다.");
        }

        if(!code.equals(emailValidReqDto.getCode())) {
            throw new BaseBizException("이메일 인증코드가 일치하지 않습니다.");
        }

        redisEmailService.updateAuthenticationStatus(emailValidReqDto.getEmail()); //상태를 "Y"로 변경
        return ApiResponse.ok(200, "이메일 인증 성공", null);
    }
}
