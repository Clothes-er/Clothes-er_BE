package com.yooyoung.clotheser.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.dto.response.TokenResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
public class AdminLoginResponse {

    @Schema(title = "이름", example = "김눈송")
    private String name;

    @Schema(title = "이메일", example = "noonsong@sookmyung.ac.kr")
    private String email;

    @Schema(title = "마지막으로 로그인한 시간", example = "2024년 06월 20일 19:13:36")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime lastLoginAt;

    private TokenResponse token;

    public AdminLoginResponse(User user, TokenResponse token) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.lastLoginAt = user.getLastLoginAt();
        this.token = token;
    }

}
