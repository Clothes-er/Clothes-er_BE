package com.yooyoung.clotheser.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class TokenRequest {

    @Schema(title = "리프레시 토큰", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1Iiwicm9sZSI6IkFETUlOIiwiaXNSZWZyZXNoVG9rZW4iOnRydWUsImlhdCI6MTcyNTg4OTgxOSwiZXhwIjoxNzI1ODg5OTk5fQ.VvVMwnoePNXXfQdJEz3AzAz_m23mWZt94yW7U_wZ9XqlmKrp3-QmALWqlwSrnc2oZEFPi000R0uKNkdT9MRK5Q")
    @NotBlank(message = "리프레시 토큰을 입력해주세요.")
    private String refreshToken;

}
