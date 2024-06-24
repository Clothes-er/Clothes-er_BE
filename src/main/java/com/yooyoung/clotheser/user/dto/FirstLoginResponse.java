package com.yooyoung.clotheser.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.user.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class FirstLoginResponse {

    @Schema(title = "이름", example = "김눈송")
    private String name;

    @Schema(title = "닉네임", example = "눈송이")
    private String nickname;

    @Schema(title = "위도", example = "37.602354")
    private double latitude;

    @Schema(title = "경도", example = "127.026905")
    private double longitude;

    @Schema(title = "성별", example = "FEMALE")
    private Gender gender;

    @Schema(title = "키", example = "160")
    private Integer height;
    @Schema(title = "몸무게", example = "48")
    private Integer weight;
    @Schema(title = "발 사이즈", example = "240")
    private Integer shoeSize;

    @Schema(title = "체형 목록", type = "array", example = "[\"어깨가 넓다\"]")
    private List<String> bodyShapes;
    @Schema(title = "카테고리 목록", type = "array", example = "[\"셔츠\", \"블라우스\"]")
    private List<String> categories;
    @Schema(title = "스타일 목록", type = "array", example = "[\"하이틴\", \"캐주얼\", \"키치\"]")
    private List<String> styles;

    @Schema(title = "회원가입한 시간", example = "2024년 06월 20일 19:13:36")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    @Schema(title = "회원 정보 수정한 시간", example = "2024년 06월 20일 19:13:36")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public FirstLoginResponse(User user, List<String> bodyShapes,
                              List<String> categories, List<String> styles) {
        this.name = user.getName();
        this.nickname = user.getNickname();

        this.latitude = user.getLatitude();
        this.longitude = user.getLongitude();

        this.gender = user.getGender();
        this.height = user.getHeight();
        this.weight = user.getWeight();
        this.shoeSize = user.getShoeSize();

        this.bodyShapes = bodyShapes;
        this.categories = categories;
        this.styles = styles;

        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

}
