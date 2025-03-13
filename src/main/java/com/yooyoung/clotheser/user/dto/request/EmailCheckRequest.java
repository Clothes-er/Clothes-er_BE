package com.yooyoung.clotheser.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Builder
public class EmailCheckRequest {

    @Schema(title = "이메일", description = "255자 이내", example = "noonsong@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일을 입력해주세요.")
    @Pattern(regexp = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,}$",
            message = "이메일 형식이 맞지 않습니다.")
    private String email;

    @Schema(title = "인증 번호", description = "6자리 숫자", example = "195021", requiredMode = Schema.RequiredMode.REQUIRED)
    private int authCode;

}
