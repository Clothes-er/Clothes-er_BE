package com.yooyoung.clotheser.user.dto;

import com.yooyoung.clotheser.user.domain.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserProfileRequest {

    @Schema(title = "성별", example = "FEMALE", requiredMode = Schema.RequiredMode.REQUIRED)
    private Gender gender;
    @Schema(title = "키", description = "0 또는 자연수", example = "165", requiredMode = Schema.RequiredMode.REQUIRED)
    @PositiveOrZero(message = "키는 0 또는 자연수여야 합니다.")
    private Integer height;

    @Schema(title = "몸무게", description = "0 또는 자연수", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
    @PositiveOrZero(message = "몸무게는 0 또는 자연수여야 합니다.")
    private Integer weight;

    @Schema(title = "발 사이즈", description = "0 또는 자연수", example = "235", requiredMode = Schema.RequiredMode.REQUIRED)
    @PositiveOrZero(message = "발 사이즈는 0 또는 자연수여야 합니다.")
    private Integer shoeSize;

    @Schema(title = "체형 목록", description = "30자 이내", type = "array", example = "[\"골반이 넓음\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> bodyShapes;
    @Schema(title = "카테고리 목록", description = "10자 이내", type = "array", example = "[\"니트\", \"청바지\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> categories;
    @Schema(title = "스타일 목록", description = "20자 이내", type = "array", example = "[\"키치\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> styles;

}
