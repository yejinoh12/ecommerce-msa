package com.userservice.controller;


import com.common.utils.ParseRequestUtil;
import com.userservice.dto.SignUpRequestDto;
import com.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/profile")
    public ResponseEntity<?> myPage(HttpServletRequest request){
        Long userId = new ParseRequestUtil().extractUserIdFromRequest(request);
        return ResponseEntity.status(HttpStatus.OK).body(userService.myPage(userId));
    }
}
