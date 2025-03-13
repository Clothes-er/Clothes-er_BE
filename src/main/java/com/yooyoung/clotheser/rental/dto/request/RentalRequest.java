package com.yooyoung.clotheser.rental.dto.request;

import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.dto.RentalPriceDto;
import com.yooyoung.clotheser.user.domain.Gender;
import com.yooyoung.clotheser.user.domain.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RentalRequest {

    @Schema(title = "보유 옷 id", description = "기존 보유 옷과 연결할 경우 입력", example = "1")
    private Long clothesId;

    @Schema(title = "제목", description = "50자 이내", example = "스퀘어 블라우스", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 50, message = "제목은 50자 이내로 입력해주세요.")
    private String title;

    @Schema(title = "상세 설명", description = "500자 이내", example = "여름에 입기 너무 좋아요!\n새로운 스타일을 도전해보세요~", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "상세 설명을 입력해주세요.")
    @Size(max = 500, message = "상세 설명은 500자 이내로 입력해주세요.")
    private String description;

    @Schema(title = "성별", example = "FEMALE")
    private Gender gender;
    @Schema(title = "카테고리", description = "10자 이내", example = "블라우스")
    private String category;
    @Schema(title = "스타일", description = "20자 이내", example = "러블리")
    private String style;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid      // 리스트의 각 요소 유효성 검사 목적
    @NotEmpty(message = "가격 정보를 입력해주세요.")
    private List<RentalPriceDto> prices;

    @Schema(title = "브랜드", description = "20자 이내", example = "Roem")
    private String brand;
    @Schema(title = "사이즈", description = "10자 이내 (숫자도 가능)", example = "FREE")
    private String size;
    @Schema(title = "핏", description = "10자 이내", example = "정핏")
    private String fit;

    public Rental toEntity(User user) {
        return Rental.builder()
                .user(user)
                .clothesId(clothesId)
                .title(title)
                .description(description)
                .gender(gender)
                .category(category)
                .style(style)
                .brand(brand)
                .size(size)
                .fit(fit)
                .build();
    }

}
