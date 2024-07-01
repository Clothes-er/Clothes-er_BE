package com.yooyoung.clotheser.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Setter(AccessLevel.NONE)
public class EmailResponse {

    @Schema(title = "인증 번호", description = "6자리 랜덤 숫자 (FE 확인용)", example = "602251")
    private int authCode;

    public EmailResponse(int authCode) {
        this.authCode = authCode;
    }

}
