package com.yooyoung.clotheser.user.dto;

import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class UserProfileResponse {

    @Schema(title = "닉네임", example = "숙명이")
    private String nickname;

    @Schema(title = "프로필 사진", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/profiles/noonsong.png")
    private String profileUrl;

    @Schema(title = "레벨", example = "2")
    private int level;
    @Schema(title = "대여 횟수", example = "4")
    private int rentalCount;

    @Schema(title = "키", example = "160")
    private Integer height;
    @Schema(title = "몸무게", example = "48")
    private Integer weight;
    @Schema(title = "발 사이즈", example = "240")
    private Integer shoeSize;

    @Schema(title = "체형 목록", type = "array", example = "[\"어깨가 넓음\"]")
    private List<String> bodyShapes;
    @Schema(title = "카테고리 목록", type = "array", example = "[\"셔츠\", \"블라우스\"]")
    private List<String> categories;
    @Schema(title = "스타일 목록", type = "array", example = "[\"하이틴\", \"캐주얼\", \"키치\"]")
    private List<String> styles;

    public UserProfileResponse(User user, List<String> bodyShapes, List<String> categories, List<String> styles) {
        this.nickname = user.getNickname();
        this.profileUrl = user.getProfileUrl();

        this.level = user.getUserLevel();
        this.rentalCount = user.getRentalCount();

        this.height = user.getHeight();
        this.weight = user.getWeight();
        this.shoeSize = user.getShoeSize();

        this.bodyShapes = bodyShapes;
        this.categories = categories;
        this.styles = styles;
    }

}
