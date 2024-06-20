package com.yooyoung.clotheser.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginRequest {

    @Schema(title = "이메일", description = "255자 이내", example = "noonsong@sm.ac.kr", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @Schema(title = "비밀번호", description = "255자 이내", example = "noonsong123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

}
