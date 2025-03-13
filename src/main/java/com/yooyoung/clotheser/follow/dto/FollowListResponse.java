package com.yooyoung.clotheser.follow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Builder
@Data
@Setter(AccessLevel.NONE)
public class FollowListResponse {
    @Schema(title = "암호화된 회원 id", example = "M0h1QXdzUlVzNkRwckdUeUEvbjVQZz09")
    private String userSid;

    @Schema(title = "프로필 사진", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/profiles/noonsong.png")
    private String profileUrl;

    @Schema(title = "닉네임", example = "눈송이")
    private String nickname;

    @Schema(title = "팔로잉 여부", example = "true")
    private Boolean isFollowing;
}
