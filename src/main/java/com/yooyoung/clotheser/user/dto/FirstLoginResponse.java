package com.yooyoung.clotheser.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.user.domain.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class FirstLoginResponse {

    private String name;
    private String nickname;

    private Double latitude;
    private Double longitude;

    private Gender gender;
    private Integer height;
    private Integer weight;
    private Integer shoeSize;

    private List<String> bodyShapes;
    private List<String> categories;
    private List<String> styles;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

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
