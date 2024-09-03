package com.userservice.controller;

import com.common.dto.user.UserInfoDto;
import com.common.utils.ParseRequestUtil;
import com.userservice.dto.SignUpReqDto;
import com.userservice.service.token.TokenService;
import com.userservice.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    //회원 가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpReqDto signUpReqDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.signup(signUpReqDto));
    }

    //마이페이지
    @GetMapping("/info")
    public ResponseEntity<?> myPage(HttpServletRequest request){
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(userService.myPage(userId));
    }

    //리프레시 토큰으로 액세스토큰 발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(tokenService.refreshAccessToken(request, response));
    }

    //주문 서비스 유저 정보 조회
    @GetMapping("/info-order")
    public ResponseEntity<UserInfoDto> getUserInfo(@RequestHeader("X-Claim-userId") Long userId){
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }
}