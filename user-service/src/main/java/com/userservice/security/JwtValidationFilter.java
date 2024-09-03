package com.userservice.security;

import com.common.exception.BaseBizException;
import com.userservice.service.token.TokenRedisService;
import com.userservice.util.JwtUtil;
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

@Slf4j(topic = "Jwt 유효성 검증 및 인증 객체 생성")
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenRedisService tokenRedisService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String tokenValue = request.getHeader("Authorization");

        if (StringUtils.hasText(tokenValue)) {

            try {

                tokenValue = jwtUtil.substringToken(tokenValue);             // "Bearer " 삭제

                if (!jwtUtil.validateToken(tokenValue)) {                    // 액세스 토큰 유효성 검사
                    log.error("액세스 토큰 검증 실패");
                    throw new BaseBizException("액새스 토큰이 만료 되었습니다.");
                }

                Claims info = jwtUtil.getUserInfoFromToken(tokenValue);
                String userId = info.get("userId", String.class);  // userId를 클레임에서 추출

                log.error("액세스 토큰 검증 성공");
                log.info("사용자 ID = {}" , userId);

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

