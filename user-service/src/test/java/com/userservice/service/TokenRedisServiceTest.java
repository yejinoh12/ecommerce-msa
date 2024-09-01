package com.userservice.service;

import com.userservice.service.token.TokenRedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TokenRedisServiceTest {

    @Autowired
    private TokenRedisService tokenRedisService;

    @Test
    public void testEmailUtil() {

        String userId = "user123";
        String code = "refreshToken";

        //리프레시 토큰 저장
        tokenRedisService.setRefreshToken(userId, code);

        //리프레시 토큰 조회
        String retrievedCode = tokenRedisService.getRefreshToken(userId);
        assertEquals(code, retrievedCode, "리프레시 토큰 조회가 실패했습니다.");

        //리프레시 토큰 삭제
        tokenRedisService.deleteRefreshToken(userId);

        // 리프레시 토큰 삭제 후 조회
        String removedCode = tokenRedisService.getRefreshToken(userId);
        assertNull(removedCode, "리프레시 토큰 삭제가 실패했습니다.");
    }

}