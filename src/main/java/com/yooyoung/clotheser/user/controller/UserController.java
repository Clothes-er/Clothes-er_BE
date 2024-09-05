package com.yooyoung.clotheser.user.controller;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.dto.request.*;
import com.yooyoung.clotheser.user.dto.response.*;
import com.yooyoung.clotheser.user.service.MailService;
import com.yooyoung.clotheser.user.service.PhoneService;
import com.yooyoung.clotheser.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final MailService mailService;
    private final PhoneService phoneService;

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

    @Operation(summary = "이메일 인증 번호 전송", description = "입력한 이메일에 인증 번호를 전송한다.")
    @PostMapping("/send-email")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> sendEmail(@Valid @RequestBody EmailRequest emailRequest,
                                                                 BindingResult bindingResult) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(mailService.sendEmail(emailRequest)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "이메일 인증 번호 검증", description = "입력한 인증 번호를 검증한다.")
    @PostMapping("/check-email")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> checkEmail(@Valid @RequestBody EmailCheckRequest emailCheckRequest,
                                                                       BindingResult bindingResult) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(mailService.checkEmail(emailCheckRequest)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "휴대폰 인증 번호 전송", description = "입력한 전화 번호에 인증 번호를 문자로 전송한다.")
    @PostMapping("/send-phone")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> sendPhone(@Valid @RequestBody PhoneRequest phoneRequest,
                                                                             BindingResult bindingResult) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(phoneService.sendPhone(phoneRequest)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "휴대폰 인증 번호 검증", description = "입력한 인증 번호를 검증한다.")
    @PostMapping("/check-phone")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> checkPhone(@Valid @RequestBody PhoneCheckRequest phoneCheckRequest,
                                                                       BindingResult bindingResult) {
        try {
            // 입력 유효성 검사
            if (bindingResult.hasErrors()) {
                List<FieldError> list = bindingResult.getFieldErrors();
                for(FieldError error : list) {
                    return new ResponseEntity<>(new BaseResponse<>(REQUEST_ERROR, error.getDefaultMessage()), BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(new BaseResponse<>(phoneService.checkPhone(phoneCheckRequest)), OK);
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

    @Operation(summary = "내 프로필 조회", description = "나의 프로필을 조회한다.")
    @GetMapping("/profile")
    public ResponseEntity<BaseResponse<UserProfileResponse>> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(userService.getMyProfile(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "다른 회원 프로필 조회", description = "다른 회원의 프로필을 조회한다.")
    @Parameter(name = "userSid", description = "암호화된 회원 id", example = "M0h1QXdzUlVzNkRwckdUeUEvbjVQZz09", required = true)
    @GetMapping("/profile/{userSid}")
    public ResponseEntity<BaseResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                        @PathVariable String userSid) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(userService.getProfile(user, userSid)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "개인정보 조회", description = "회원의 개인정보를 조회한다.")
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

    @Operation(summary = "주소 조회", description = "회원의 주소(위도, 경도)를 조회한다.")
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

    @Operation(summary = "주소 수정", description = "회원의 주소(위도, 경도)를 수정한다.")
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

    @Operation(summary = "프로필 수정", description = "회원의 프로필 사진과 닉네임을 수정한다.")
    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<PatchUserProfileReponse>> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                            @RequestPart(value = "image", required = false) MultipartFile image,
                                                                            @Valid @RequestPart("nickname") UserProfileRequest userProfileRequest,
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

            return new ResponseEntity<>(new BaseResponse<>(userService.updateProfile(user, image, userProfileRequest.getNickname())), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "스펙 및 취향 수정", description = "회원의 스펙과 취향을 수정한다.")
    @PatchMapping("/style")
    public ResponseEntity<BaseResponse<UserProfileResponse>> updateStyle(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                         @Valid @RequestBody UserStyleRequest userStyleRequest,
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

            return new ResponseEntity<>(new BaseResponse<>(userService.updateStyle(user, userStyleRequest)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "회원 신고", description = "회원을 신고한다.")
    @PostMapping("/report")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> reportUser(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                       @Valid @RequestBody ReportRequest reportRequest,
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

            return new ResponseEntity<>(new BaseResponse<>(userService.reportUser(user, reportRequest)), CREATED);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 한다.")
    @DeleteMapping("/withdraw")
    public ResponseEntity<BaseResponse<BaseResponseStatus>> withdrawUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.user;
            return new ResponseEntity<>(new BaseResponse<>(userService.withdrawUser(user)), OK);
        }
        catch (BaseException exception) {
            return new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus());
        }
    }

}
