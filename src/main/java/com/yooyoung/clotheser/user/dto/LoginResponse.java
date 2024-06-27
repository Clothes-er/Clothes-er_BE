package com.yooyoung.clotheser.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Setter(AccessLevel.NONE)
public class LoginResponse {

    @Schema(title = "이메일", example = "songee@naver.com")
    private String email;

    @Schema(title = "최초 로그인 여부", example = "true")
    private Boolean isFirstLogin;

    @Schema(title = "마지막으로 로그인한 시간", example = "2024년 06월 20일 19:13:36")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime lastLoginAt;

    private TokenResponse token;

    public LoginResponse(User user, TokenResponse tokenResponse) {
        this.email = user.getEmail();
        this.isFirstLogin = user.getIsFirstLogin();
        this.lastLoginAt = user.getLastLoginAt();
        this.token = tokenResponse;
    }

}
