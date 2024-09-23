package com.userservice.service;

import com.userservice.redis.RedisEmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisEmailServiceTest {

    @Autowired
    private RedisEmailService redisEmailService;

    @Test
    public void testEmailUtil() {

        String userId = "user123";
        String code = "verificationCode";

        // 이메일 인증 코드 저장
        redisEmailService.setEmailVerificationCode(userId, code);

        // 이메일 인증 코드 조회
        String retrievedCode = redisEmailService.getEmailVerificationCode(userId);
        assertEquals(code, retrievedCode, "이메일 인증 코드 조회가 실패했습니다.");

        // 이메일 인증 코드 삭제
        redisEmailService.deleteEmailInfo(userId);

        // 이메일 인증 코드 삭제 후 조회
        String removedCode = redisEmailService.getEmailVerificationCode(userId);
        assertNull(removedCode, "이메일 인증 코드 삭제가 실패했습니다.");
    }
}