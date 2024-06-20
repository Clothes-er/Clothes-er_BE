package com.yooyoung.clotheser.user.dto;

import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Setter(AccessLevel.NONE)
public class UserInfoResponse {

    @Schema(title = "프로필 사진", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/profiles/noonsong.png")
    private String profileUrl;

    @Schema(title = "이름", example = "박숙명")
    private String name;

    @Schema(title = "닉네임", example = "숙명이")
    private String nickname;

    @Schema(title = "이메일", example = "songee@naver.com")
    private String email;

    @Schema(title = "전화번호", example = "010-1234-1234")
    private String phoneNumber;

    @Schema(title = "생일", example = "1999-08-22")
    private LocalDate birthday;

    public UserInfoResponse(User user) {
        this.profileUrl = user.getProfileUrl();
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.birthday = user.getBirthday();
    }

}
