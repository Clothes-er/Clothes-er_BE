package com.yooyoung.clotheser.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class ProfileImageResponse {

    @Schema(title = "닉네임", example = "김눈송")
    private String nickname;

    @Schema(title = "이메일", example = "noonsong@gmail.com")
    private String email;

    @Schema(title = "변경된 프로필 사진", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/profiles/noonsong.png")
    private String profileUrl;

    public ProfileImageResponse(String nickname, String email, String profileUrl) {
        this.nickname = nickname;
        this.email = email;
        this.profileUrl = profileUrl;
    }

}
