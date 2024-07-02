package com.yooyoung.clotheser.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
public class ProfileImageResponse {

    @Schema(title = "닉네임", example = "김눈송")
    private String nickname;

    @Schema(title = "이메일", example = "noonsong@gmail.com")
    private String email;

    @Schema(title = "변경된 프로필 사진", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/profiles/noonsong.png")
    private String profileUrl;

    @Schema(title = "회원 정보 수정한 시간", example = "2024년 06월 20일 19:13:36")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public ProfileImageResponse(User user) {
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.profileUrl = user.getProfileUrl();
        this.updatedAt = user.getUpdatedAt();
    }

}
