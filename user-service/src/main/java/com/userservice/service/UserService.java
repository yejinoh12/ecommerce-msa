package com.userservice.service;

import com.common.exception.BaseBizException;
import com.common.response.ApiResponse;
import com.common.dto.user.UserInfoDto;
import com.userservice.entity.User;
import com.userservice.entity.UserRoleEnum;
import com.userservice.dto.SignUpReqDto;
import com.userservice.dto.UserResDto;
import com.userservice.repository.UserRepository;
import com.userservice.redis.RedisEmailService;
import com.userservice.util.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AesUtil aesUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RedisEmailService redisEmailService;

    //회원가입
    @Transactional
    public ApiResponse<String> signup(SignUpReqDto signUpReqDto) {

        //이메일 암호화
        String email = aesUtil.encrypt(signUpReqDto.getEmail());

        //이메일 중복 검증
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BaseBizException("사용중인 이메일입니다.", HttpStatus.CONFLICT);
        }

        //이메일 인증 여부 확인
        String status = redisEmailService.getAuthenticationStatus(signUpReqDto.getEmail());
        if (status == null || !status.equals("Y")) {
            throw new BaseBizException("이메일 인증 후 시도해 주세요.");
        }

        //유저 객체 생성
        User user = User.builder()
                .name(aesUtil.encrypt(signUpReqDto.getName()))
                .password(passwordEncoder.encode(signUpReqDto.getPassword()))
                .address(aesUtil.encrypt(signUpReqDto.getAddress()))
                .email(aesUtil.encrypt(signUpReqDto.getEmail()))
                .phone(aesUtil.encrypt(signUpReqDto.getPhoneNumber()))
                .role(UserRoleEnum.USER)
                .build();

        //유저 저장
        userRepository.save(user);

        //이메일 인증 정보 삭제
        redisEmailService.deleteEmailInfo(signUpReqDto.getEmail());

        return ApiResponse.ok(201, "회원 가입 성공", null);
    }

    //회원 조회
    public ApiResponse<UserResDto> getUserProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseBizException("userID가 " + userId + "인 사용자를 찾을 수 없습니다."));

        UserResDto userResDto = UserResDto.builder()
                .name(aesUtil.decrypt(user.getName()))
                .email(aesUtil.decrypt(user.getEmail()))
                .phoneNumber(aesUtil.decrypt(user.getPhone()))
                .address(aesUtil.decrypt(user.getAddress()))
                .createdAt(user.getCreatedAt())
                .modifiedAt(user.getModifiedAt())
                .build();

        return ApiResponse.ok(200, "회원 조회 성공", userResDto);
    }

    //주문 서비스 유저 정보 조회
    public UserInfoDto getUserInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseBizException("userID가 " + userId + "인 사용자를 찾을 수 없습니다."));

        return UserInfoDto.builder()
                .name(aesUtil.decrypt(user.getName()))
                .email(aesUtil.decrypt(user.getEmail()))
                .phone(aesUtil.decrypt(user.getPhone()))
                .build();
    }
}