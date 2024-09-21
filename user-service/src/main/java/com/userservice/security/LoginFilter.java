package com.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservice.entity.UserRoleEnum;
import com.userservice.dto.LoginReqDto;
import com.userservice.redis.RefreshTokeRedis;
import com.userservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j(topic = "로그인 필터")
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final RefreshTokeRedis refreshTokenService;

    public LoginFilter(JwtUtil jwtUtil, RefreshTokeRedis refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        setFilterProcessesUrl("/user/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {

        log.info("로그인 시도");

        try {

            LoginReqDto requestDto = new ObjectMapper().readValue(req.getInputStream(), LoginReqDto.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getEmail(),
                            requestDto.getPassword(),
                            null
                    )
            );

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        log.info("로그인 성공 및 JWT 생성");

        String userId = ((UserDetailsImpl) authResult.getPrincipal()).getUserId();
        UserRoleEnum role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getRole();
        log.info("user ID = {}", userId);

        //엑세스 토큰 & 리프레시 토큰 발급
        String accessToken = jwtUtil.createAccessToken(userId, role);
        String refreshToken = jwtUtil.createRefreshToken(userId);

        refreshTokenService.setRefreshToken(userId, refreshToken);
        response.setHeader(JwtUtil.AUTHORIZATION_HEADER, accessToken);
        jwtUtil.addRefreshTokenToCookie(refreshToken, response);

        log.info("액세스 토큰 & 리프레시 토큰 발급 완료");
        successMessage(response, accessToken, refreshToken);

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws UsernameNotFoundException, IOException, ServletException {
        log.info("로그인 실패");
        response.setStatus(401);
        errorMessage(response, "로그인에 실패하셨습니다.");
    }

    //응답 메시지

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

    private void successMessage(HttpServletResponse response, String access, String refresh) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(
                "{"
                        + "\"success\": true,"
                        + "\"accessToken\": \"" + access + "\","
                        + "\"refreshToken\": \"" + refresh + "\""
                        + "}"
        );
    }
}