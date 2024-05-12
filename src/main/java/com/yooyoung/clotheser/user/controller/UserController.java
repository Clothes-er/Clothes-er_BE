package com.yooyoung.clotheser.user.controller;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.user.dto.FirstLoginRequest;
import com.yooyoung.clotheser.user.dto.FirstLoginResponse;
import com.yooyoung.clotheser.user.dto.SignUpRequest;
import com.yooyoung.clotheser.user.dto.SignUpResponse;
import com.yooyoung.clotheser.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest signUpRequest,
                                                               BindingResult bindingResult) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            // 비밀번호 재입력 일치 확인
            if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmedPassword())) {
                throw new BaseException(PASSWORD_CONFIRMATION_MISMATCH, BAD_REQUEST);
            }

            return new ResponseEntity<>(new BaseResponse<>(userService.signUp(signUpRequest)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    // 닉네임 중복 확인
    @GetMapping("/check-nickname/{nickname}")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> checkNickname(@PathVariable String nickname) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(userService.checkNickname(nickname)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    // TODO: 토큰 적용
    // 최초 로그인
    @PostMapping("/first-login")
    public ResponseEntity<BaseResponse<FirstLoginResponse>> firstLogin(@Valid @RequestBody FirstLoginRequest firstLoginRequest,
                                                                       BindingResult bindingResult) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(userService.firstLogin(firstLoginRequest)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    // 로그인

    // 로그아웃

    // 회원 프로필 조회

    // 회원 정보 조회
}
