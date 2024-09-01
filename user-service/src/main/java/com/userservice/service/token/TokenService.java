package com.userservice.service.token;

import com.common.response.ApiResponse;
import com.userservice.dto.TokenResDto;
import com.userservice.entity.UserRoleEnum;
import com.userservice.exeption.AuthenticationException;
import com.userservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;
    private final TokenRedisService tokenRedisService;

    public ApiResponse<TokenResDto> refreshAccessToken(HttpServletRequest request, HttpServletResponse response){

        //쿠키에서 리프레시 토큰 가져오기
        String refreshToken = jwtUtil.getRefreshTokenFromRequest(request);
        refreshToken = jwtUtil.substringToken(refreshToken);

        //레디스에서 리프레시 토큰 가져오기
        String userId = jwtUtil.getUserInfoFromToken(refreshToken).getSubject();
        String refreshTokenFromRedis = tokenRedisService.getRefreshToken(userId);
        refreshTokenFromRedis = jwtUtil.substringToken(refreshTokenFromRedis);

        //유효성 검증
        if (!StringUtils.hasText(refreshToken) || !jwtUtil.validateToken(refreshToken) || !refreshTokenFromRedis.equals(refreshToken)) {
            jwtUtil.clearRefreshTokenFromCookie(response);
            throw new AuthenticationException("리프레시 토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        //엑세스 토큰 & 리프레시 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken(userId, UserRoleEnum.USER);
        String newRefreshToken = jwtUtil.createRefreshToken(userId);

        tokenRedisService.setRefreshToken(userId, refreshToken);
        response.setHeader(JwtUtil.AUTHORIZATION_HEADER, newAccessToken);
        jwtUtil.addRefreshTokenToCookie(newRefreshToken, response);

        log.info("userID = {}, 액세스 토큰 발급 완료", userId);
        return ApiResponse.ok(200, "액세스 토큰 발급 완료", new TokenResDto(newAccessToken, newRefreshToken));
    }
}

