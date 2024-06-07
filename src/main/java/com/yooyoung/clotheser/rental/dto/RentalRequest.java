package com.yooyoung.clotheser.rental.dto;

import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.user.domain.Gender;
import com.yooyoung.clotheser.user.domain.User;

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

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 20, message = "제목은 20자 이내로 입력해주세요.")
    private String title;

    @NotBlank(message = "상세 설명을 입력해주세요.")
    @Size(max = 500, message = "상세 설명은 500자 이내로 입력해주세요.")
    private String description;

    private Gender gender;
    private String category;
    private String style;

    @Valid
    @NotEmpty(message = "가격 정보를 입력해주세요.")
    private List<RentalPriceDto> prices;

    private String brand;
    private String size;
    private String fit;

    public Rental toEntity(User user) {
        return Rental.builder()
                .user(user)
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
