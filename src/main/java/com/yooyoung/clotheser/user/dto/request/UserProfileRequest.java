package com.yooyoung.clotheser.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Setter(AccessLevel.NONE)
public class UserProfileRequest {

    @Schema(title = "닉네임", description = "2 ~ 10자", example = "닉네임", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 10, message = "닉네임은 2 ~ 10자로 입력해주세요.")
    private String nickname;

}
