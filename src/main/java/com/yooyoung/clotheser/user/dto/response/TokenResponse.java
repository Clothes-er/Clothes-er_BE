package com.yooyoung.clotheser.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
public class TokenResponse {

    @Schema(title = "JWT 인증 타입", defaultValue = "Bearer", example = "Bearer")
    private String grantType;

    @Schema(title = "액세스 토큰", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1Iiwicm9sZSI6IkFETUlOIiwiaWF0IjoxNzI1ODg5ODE5LCJleHAiOjE3MjU4ODk4Nzl9.O7yxYqms0Uxc48hWLvn6DMcfIoJ3xhrbziR5ebOiVNfESSzu8UBgmZspbhHKDHpiyrGPrqiQryLJSzNuCyj33w")
    private String accessToken;

    @Schema(title = "리프레시 토큰", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1Iiwicm9sZSI6IkFETUlOIiwiaXNSZWZyZXNoVG9rZW4iOnRydWUsImlhdCI6MTcyNTg4OTgxOSwiZXhwIjoxNzI1ODg5OTk5fQ.VvVMwnoePNXXfQdJEz3AzAz_m23mWZt94yW7U_wZ9XqlmKrp3-QmALWqlwSrnc2oZEFPi000R0uKNkdT9MRK5Q")
    private String refreshToken;

}
