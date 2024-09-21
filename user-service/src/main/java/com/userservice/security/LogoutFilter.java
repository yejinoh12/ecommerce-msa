package com.userservice.security;

import com.userservice.redis.BlacklistRedis;
import com.userservice.redis.RefreshTokeRedis;
import com.userservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutFilter implements LogoutHandler, LogoutSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokeRedis refreshTokeRedis;
    private final BlacklistRedis blacklistRedis;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        // 1. 액세스 토큰을 레디스에 저장
        String accessToken = request.getHeader("Authorization");
        accessToken = jwtUtil.substringToken(accessToken);
        long accessTokenExpirationTime = jwtUtil.getAccessTokenExpirationTime(accessToken);
        blacklistRedis.addTokenToBlacklist(accessToken, accessTokenExpirationTime);

        // 2. 리프레시 토큰을 쿠키에서 삭제
        jwtUtil.clearRefreshTokenFromCookie(response);

        // 3. 리프레시 토큰을 레디스에서 삭제
        Claims info = jwtUtil.getClaimFromToken(accessToken);
        String userId = info.get("userId", String.class);
        refreshTokeRedis.deleteRefreshToken(userId);
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println("{\"message\": \"Logout successful\"}");
    }


}