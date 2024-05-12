package com.yooyoung.clotheser.user.dto;

import com.yooyoung.clotheser.user.domain.Gender;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FirstLoginRequest {

    // TODO: 토큰 적용하면 없애기
    private Long userId;

    @NotNull(message = "위도를 입력해주세요.")
    private Double latitude;

    @NotNull(message = "경도를 입력해주세요.")
    private Double longitude;

    private Gender gender;
    // null: 사용자가 추가 안 함
    private Integer height;
    private Integer weight;
    private Integer shoeSize;

    private List<String> bodyShapes;
    private List<String> categories;
    private List<String> styles;

}
