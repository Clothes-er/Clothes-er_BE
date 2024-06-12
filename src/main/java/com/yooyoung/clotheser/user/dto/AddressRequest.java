package com.yooyoung.clotheser.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressRequest {

    @NotNull(message = "위도를 입력해주세요.")
    private Double latitude;

    @NotNull(message = "경도를 입력해주세요.")
    private Double longitude;
}
