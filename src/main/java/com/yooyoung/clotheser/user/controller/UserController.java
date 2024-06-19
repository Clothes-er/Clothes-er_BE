package com.yooyoung.clotheser.user.controller;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.dto.*;
import com.yooyoung.clotheser.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "회원 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "회원가입을 한다.")
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

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 중복 여부를 확인한다.")
    @Parameter(name = "nickname", description = "닉네임", example = "김눈송", required = true)
    @GetMapping("/check-nickname/{nickname}")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> checkNickname(@PathVariable String nickname) {
        try {
            return new ResponseEntity<>(new BaseResponse<>(userService.checkNickname(nickname)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "로그인", description = "로그인을 하여 JWT를 얻는다.")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest,
                                                             BindingResult bindingResult) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(userService.login(loginRequest)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    // TODO: 액세스 토큰 재발급
    /*@PostMapping("/token/refresh")
    public ResponseEntity<BaseResponse<TokenResponse>> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        try {

            return new ResponseEntity<>(new BaseResponse<>(userService.refreshToken(refreshToken)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }*/

    @Operation(summary = "최초 로그인", description = "최초 로그인을 한다.")
    @PostMapping("/first-login")
    public ResponseEntity<BaseResponse<FirstLoginResponse>> firstLogin(@Valid @RequestBody FirstLoginRequest firstLoginRequest,
                                                                       BindingResult bindingResult,
                                                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;

            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(userService.firstLogin(firstLoginRequest, user)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    // TODO: 로그아웃

    @Operation(summary = "회원 프로필 조회", description = "회원의 프로필을 조회한다.")
    @GetMapping("/profile")
    public ResponseEntity<BaseResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(userService.getProfile(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "회원 개인정보 조회", description = "회원의 개인정보를 조회한다.")
    @GetMapping("/info")
    public ResponseEntity<BaseResponse<UserInfoResponse>> getUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(userService.getUserInfo(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "회원 주소 조회", description = "회원의 주소(위도, 경도)를 조회한다.")
    @GetMapping("/address")
    public ResponseEntity<BaseResponse<AddressResponse>> getAddress(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(userService.getAddress(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "회원 주소 수정", description = "회원의 주소(위도, 경도)를 수정한다.")
    @PatchMapping("/address")
    public ResponseEntity<BaseResponse<AddressResponse>> updateAddress(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                       @Valid @RequestBody AddressRequest addressRequest,
                                                                       BindingResult bindingResult) {
        try {
            User user = userDetails.user;

            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(userService.updateAddress(user, addressRequest)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

}
