package com.userservice.util;

import com.common.exception.BaseBizException;
import com.userservice.entity.UserRoleEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "RefreshToken";
    public static final String BEARER_PREFIX = "Bearer ";

    //만료 시간
    private final long jwtExpiration = 24 * 60 * 60 * 1000L;               // 1일 (24시간)
    private final long refreshTokenExpiration = 14 * 24 * 60 * 60 * 1000L; // 2주 (14일)

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    //액세스 토큰 생성
    public String createAccessToken(String userId, UserRoleEnum role) {

        Date date = new Date();

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(userId)                                          // 사용자 식별자값(ID)
                        .setClaims(claims)
                        .setExpiration(new Date(date.getTime() + jwtExpiration))    // 만료 시간
                        .setIssuedAt(date)                                          // 발급일
                        .signWith(key, signatureAlgorithm)                          // 암호화 알고리즘
                        .compact();
    }

    //리프레시 토큰 발급
    public String createRefreshToken(String userId) {

        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(userId)                                                 // 사용자 식별자값(ID)
                        .setExpiration(new Date(date.getTime() + refreshTokenExpiration))   // 만료 시간
                        .setIssuedAt(date)                                                  // 발급일
                        .signWith(key, signatureAlgorithm)                                  // 암호화 알고리즘
                        .compact();
    }

    //쿠키에 리프레시 토큰 추가
    public void addRefreshTokenToCookie(String token, HttpServletResponse res) {

        try {

            token = URLEncoder.encode(token, "utf-8").replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행

            Cookie refreshCookie = new Cookie(REFRESH_TOKEN_HEADER, token); // Name-Value

            refreshCookie.setHttpOnly(true);    // JavaScript를 통한 접근 방지
            refreshCookie.setSecure(true);      // HTTPS를 통해서만 쿠키 전송
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge((int) refreshTokenExpiration); // 2주

            res.addCookie(refreshCookie);

        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
    }

    //쿠키에 있는 리프레시 토큰 삭제(검증 실패 시)
    public void clearRefreshTokenFromCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_HEADER, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    //쿠키에서 리프레시 토큰 추출
    public String getRefreshTokenFromRequest(HttpServletRequest req) {

        Cookie[] cookies = req.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(REFRESH_TOKEN_HEADER)) {
                    try {
                        return URLDecoder.decode(cookie.getValue(), "UTF-8"); //다시 decode
                    } catch (UnsupportedEncodingException e) {
                        return null;
                    }
                }
            }
        }

        return null;
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.info("유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    // JWT 토큰 substring
    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new BaseBizException("유효한 토큰이 없습니다. 다시 로그인 해주세요.", HttpStatus.BAD_REQUEST);
    }

    //토큰에서 claim 추출
    public Claims getClaimFromToken(String substringToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(substringToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // 토큰 만료 시간을 추출 (현재 시간과 만료 시간의 차이)
    public long getAccessTokenExpirationTime(String substringToken) {
        try {
            Claims claims = getClaimFromToken(substringToken);
            log.info(claims.getExpiration().toString());
            return claims.getExpiration().getTime() - System.currentTimeMillis();
        } catch (ExpiredJwtException e) {
            return 0L;
        } catch (Exception e) {
            return 6000L; //기본 값
        }
    }
}