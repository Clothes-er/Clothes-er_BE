package com.yooyoung.clotheser.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressRequest {

    @Schema(title = "위도", example = "37.603715", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "위도를 입력해주세요.")
    private Double latitude;

    @Schema(title = "경도", example = "127.019877", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "경도를 입력해주세요.")
    private Double longitude;
}
