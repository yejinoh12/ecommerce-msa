package com.userservice.security;

import com.userservice.entity.UserRoleEnum;
import com.userservice.exeption.JwtAuthenticationException;
import com.userservice.util.JwtUtil;
import com.userservice.util.RedisUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String tokenValue = request.getHeader("Authorization");

        if (StringUtils.hasText(tokenValue)) {

            try {
                tokenValue = jwtUtil.substringToken(tokenValue);             // "Bearer " 삭제
                log.info("TOKEN = {} ", tokenValue);

                if (!jwtUtil.validateToken(tokenValue)) {                    // 액세스 토큰 유효성 검사

                    log.error("액세스 토큰 검증 실패");
                    refreshAccessToken(request, response);                  // AccessToken 검증 실패 시 refreshToken 검증
                    return;

                }

                Claims info = jwtUtil.getUserInfoFromToken(tokenValue);
                String userId = info.get("userId", String.class);  // userId를 클레임에서 추출

                log.error("액세스 토큰 검증 성공");
                log.info("액세스 토큰 = {}" , tokenValue);
                log.info("토큰에서 가져온 사용자 ID = {}" , userId);

                setAuthentication(userId);

            } catch (Exception e) {
                log.error(e.getMessage());
                errorMessage(response, e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // 인증 처리
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUserId(username);
        log.info("사용자 검증 후 인증 객체 생성 = {} " , userDetails.getUsername());
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    //refresh token 검증
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response){

        log.error("리프레시 토큰 검증 시작");

        // 헤더에 담긴 Access Token
        String expiredAccessToken = request.getHeader("Authorization");
        expiredAccessToken = jwtUtil.substringToken(expiredAccessToken);
        log.info("만료된 토큰 = {}", expiredAccessToken);

        // 사용자 유효성 검사
        String userId = jwtUtil.getUserInfoFromToken(expiredAccessToken).getSubject();
        UserDetailsImpl user = userDetailsService.loadUserByUserId(userId);
        log.info("만료된 토큰에서 가져온 사용자 ID = {}", userId);

        // 쿠키에서 Refresh Token 가져오기
        String refreshToken = jwtUtil.getRefreshTokenFromRequest(request);
        refreshToken = jwtUtil.substringToken(refreshToken);
        log.info("쿠키에서 가져온 리프레시 토큰 = {}", refreshToken);

        // Redis 에서 Refresh Token 가져오기
        String refreshTokenFromRedis = redisUtil.getRefreshToken(user.getUserId());
        refreshTokenFromRedis = jwtUtil.substringToken(refreshTokenFromRedis);
        log.info("레디스에서 가져온 리프레시 토큰 = {}", refreshTokenFromRedis);

        // Refresh Token 유효성 검증
        if (!StringUtils.hasText(refreshToken) || !jwtUtil.validateToken(refreshToken) || !refreshTokenFromRedis.equals(refreshToken)) {

            log.info("리프레시 토큰 만료 또는 유효하지 않음");
            redisUtil.deleteRefreshToken(user.getUserId()); //레디스에 있는 리프레시 토큰 삭제
            jwtUtil.clearRefreshTokenFromCookie(response);    //쿠키에서 리프레시 토큰 지우기

            throw new JwtAuthenticationException("리프레시 토큰이 유효하지 않습니다.");
        }

        // 새로운 AccessToken 발급
        log.info("새로운 액세스 토큰 발급");
        String newAccessToken = jwtUtil.createAccessToken(userId, UserRoleEnum.USER);

        // 헤더를 통해 전달
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, newAccessToken);
        log.info("재발급 후 헤더에 추가");
        log.info("새롭게 발급한 액세스 토큰 = {}", newAccessToken);

    }

    //에러 메시지
    private void errorMessage(HttpServletResponse response, String message) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(
                "{"
                        + "\"success\": false,"
                        + "\"message\": \"" + message + "\""
                        + "}"
        );
    }
}

