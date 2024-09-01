package com.userservice.service;

import com.userservice.service.email.EmailRedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmailRedisServiceTest {

    @Autowired
    private EmailRedisService emailRedisService;

    @Test
    public void testEmailUtil() {

        String userId = "user123";
        String code = "verificationCode";

        // 이메일 인증 코드 저장
        emailRedisService.setEmailVerificationCode(userId, code);

        // 이메일 인증 코드 조회
        String retrievedCode = emailRedisService.getEmailVerificationCode(userId);
        assertEquals(code, retrievedCode, "이메일 인증 코드 조회가 실패했습니다.");

        // 이메일 인증 코드 삭제
        emailRedisService.deleteEmailInfo(userId);

        // 이메일 인증 코드 삭제 후 조회
        String removedCode = emailRedisService.getEmailVerificationCode(userId);
        assertNull(removedCode, "이메일 인증 코드 삭제가 실패했습니다.");
    }
}