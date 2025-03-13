package com.yooyoung.clotheser.clothes.dto;

import com.yooyoung.clotheser.clothes.domain.Clothes;
import com.yooyoung.clotheser.user.domain.Gender;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class ClothesRequest {

    @Schema(title = "대여글 id", description = "기존 대여글과 연결할 경우 입력", example = "1")
    private Long rentalId;

    @Schema(title = "상품명", description = "50자 이내", example = "스퀘어 아이보리 블라우스", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "상품명을 입력해주세요.")
    @Size(max = 50, message = "상품명은 50자 이내로 입력해주세요.")
    private String name;

    @Schema(title = "옷 후기", description = "500자 이내", example = "허리 라인에 밴딩이 있어서 활동성이 좋아요!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "옷 후기를 입력해주세요.")
    @Size(max = 500, message = "옷 후기는 500자 이내로 입력해주세요.")
    private String description;

    @Schema(title = "성별", example = "FEMALE")
    private Gender gender;

    @Schema(title = "카테고리", description = "10자 이내", example = "블라우스")
    private String category;

    @Schema(title = "스타일", example = "캐주얼")
    private String style;

    @Schema(title = "구매 가격", example = "19900")
    private Integer price;

    @Schema(title = "브랜드", description = "20자 이내", example = "에이블리")
    private String brand;

    @Schema(title = "사이즈", description = "10자 이내 (숫자도 가능)", example = "FREE")
    private String size;

    @Schema(title = "구매처 링크", example = "https://m.a-bly.com/goods/7995907")
    private String shoppingUrl;

    @Schema(title = "공개 여부", example = "true", defaultValue = "true")
    private Boolean isPublic = true;

    public Clothes toEntity(User user) {
        return Clothes.builder()
                .rentalId(rentalId)
                .user(user)
                .name(name)
                .description(description)
                .gender(gender)
                .category(category)
                .style(style)
                .price(price)
                .brand(brand)
                .size(size)
                .shoppingUrl(shoppingUrl)
                .isPublic(isPublic)
                .build();
    }

}
