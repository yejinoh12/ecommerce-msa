package com.userservice.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    public void testGetAccessTokenExpirationTime() {

        // 토큰 생성
        String userId = "testUserId";
        String accessToken = jwtUtil.createAccessToken(userId, null);
        String substringToken = jwtUtil.substringToken(accessToken);

        // 만료 시간 확인
        long expirationTime = jwtUtil.getAccessTokenExpirationTime(substringToken);

        System.out.println(expirationTime);

        // 만료 시간은 현재 시간과 만료 시간의 차이로 계산되므로 0보다 큰 값을 기대
        //assertEquals(true, expirationTime > 0);
    }
}