package com.yooyoung.clotheser.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter(AccessLevel.NONE)
public class LoginResponse {

    @Schema(title = "이메일", example = "songee@naver.com")
    private String email;

    @Schema(title = "최초 로그인 여부", example = "true")
    private Boolean isFirstLogin;

    private TokenResponse token;

}
