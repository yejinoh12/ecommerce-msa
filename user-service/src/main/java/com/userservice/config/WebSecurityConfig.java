package com.userservice.config;

import com.userservice.security.LoginFilter;
import com.userservice.security.JwtValidationFilter;
import com.userservice.security.LogoutFilter;
import com.userservice.security.UserDetailsServiceImpl;
import com.userservice.redis.BlacklistRedis;
import com.userservice.redis.RefreshTokeRedis;
import com.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtUtil jwtUtil;
    private final RefreshTokeRedis refreshTokenService;
    private final BlacklistRedis blacklistRedis;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public LoginFilter jwtAuthenticationFilter() throws Exception {
        LoginFilter filter = new LoginFilter(jwtUtil, refreshTokenService);
        filter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
        return filter;
    }

    @Bean
    public JwtValidationFilter jwtAuthorizationFilter() {
        return new JwtValidationFilter(jwtUtil, blacklistRedis, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF 설정
        http.csrf((csrf) -> csrf.disable());

        //Form 로그인 방식 disable
        http.formLogin((auth) -> auth.disable());

        //http basic 인증 방식 disable
        http.httpBasic((auth) -> auth.disable());

        // 기본 설정인 Session 방식은 사용하지 않고 JWT 방식을 사용하기 위한 설정 //서버가 세션을 생성하거나 관리하지 않도록
        http.sessionManagement((sessionManagement) ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        //API gateway 사용으로 모든 엔드포인트 접근 허용
        http.authorizeHttpRequests((authorizeHttpRequests) ->
                authorizeHttpRequests
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
        );

        //로그아웃 필터
        http.logout(logoutConfigurer ->
                        logoutConfigurer
                                .addLogoutHandler(new LogoutFilter(jwtUtil, refreshTokenService, blacklistRedis))
                                .logoutSuccessHandler(new LogoutFilter(jwtUtil, refreshTokenService, blacklistRedis))
                                .logoutUrl("/user/logout")
                                .invalidateHttpSession(true)
                                .permitAll()
                );

        // 필터
        http.addFilterBefore(jwtAuthorizationFilter(), LoginFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}