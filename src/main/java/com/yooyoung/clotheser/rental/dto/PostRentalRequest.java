package com.yooyoung.clotheser.rental.dto;

import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.user.domain.Gender;
import com.yooyoung.clotheser.user.domain.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PostRentalRequest {

    private List<String> imgUrls;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 20, message = "제목은 20자 이내로 입력해주세요.")
    private String title;

    @NotBlank(message = "상세 설명을 입력해주세요.")
    @Size(max = 500, message = "상세 설명은 500자 이내로 입력해주세요.")
    private String description;

    private Gender gender;
    private String category;
    private String style;

    private List<RentalPriceDto> prices;

    private String brand;
    private String size;
    private String fit;

    public Rental toEntity(User user, Long clothesId) {
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
