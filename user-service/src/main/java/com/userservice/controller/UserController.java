package com.userservice.controller;

import com.common.dto.user.UserInfoDto;
import com.common.utils.ParseRequestUtil;
import com.userservice.dto.SignUpRequestDto;
import com.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    //회원 가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequestDto signUpRequestDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.signup(signUpRequestDto));
    }

    //마이페이지
    @GetMapping("/info")
    public ResponseEntity<?> myPage(HttpServletRequest request){
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.ok(userService.myPage(userId));
    }

    //order-service 에서 유저 ID로 유저 정보 조회
    @GetMapping("/info-order")
    public ResponseEntity<UserInfoDto> getUserInfo(@RequestHeader("X-Claim-userId") Long userId){
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }
}