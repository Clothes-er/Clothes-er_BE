package com.yooyoung.clotheser.rental.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RentalPriceDto {

    @Schema(title = "일수", description = "0 또는 자연수", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "일수를 입력해주세요.")
    @PositiveOrZero(message = "일수는 0 또는 자연수여야 합니다.")
    private Integer days;

    @Schema(title = "가격", description = "0 또는 자연수", example = "3000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "가격을 입력해주세요.")
    @PositiveOrZero(message = "가격은 0 또는 자연수여야 합니다.")
    private Integer price;

    public RentalPriceDto(Integer days, Integer price) {
        this.days = days;
        this.price = price;
    }

}
