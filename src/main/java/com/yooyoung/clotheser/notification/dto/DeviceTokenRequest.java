package com.yooyoung.clotheser.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class DeviceTokenRequest {
    @Schema(title = "디바이스 토큰", description = "255자 이내", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "디바이스 토큰을 입력해주세요.")
    private String deviceToken;
}
