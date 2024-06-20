package com.yooyoung.clotheser.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
public class SignUpResponse {

    @Schema(title = "이름", example = "박숙명")
    private String name;

    @Schema(title = "닉네임", example = "숙명이")
    private String nickname;

    @Schema(title = "이메일", example = "songee@naver.com")
    private String email;

    @Schema(title = "비밀번호", example = "songee123!")
    private String password;

    @Schema(title = "생일", example = "1999-08-22")
    private LocalDate birthday;

    @Schema(title = "전화번호", example = "010-1234-1234")
    private String phoneNumber;

    @Schema(title = "회원가입한 시간", example = "2024-06-20 22:54:36")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public SignUpResponse(User user) {
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.birthday = user.getBirthday();
        this.phoneNumber = user.getPhoneNumber();
        this.createdAt = user.getCreatedAt();
    }

}
