package com.yooyoung.clotheser.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
public class TokenResponse {

    @Schema(title = "JWT 인증 타입", defaultValue = "Bearer", example = "Bearer")
    private String grantType;

    @Schema(title = "액세스 토큰", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1Iiwicm9sZSI6IkFETUlOIiwiaWF0IjoxNzI0NjYzMDkxLCJleHAiOjE3MjQ3NDk0OTF9.JSDmjsejlskMakCKLRR4jiSnv6f3wW9JvmV_mWlkfnKTEe_qsRcpaagNnDItHcRury3tC1Jt3iVyYSftazt1_w")
    private String accessToken;

    @Schema(title = "리프레시 토큰", example = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE3MjEzNjg2MDZ9.25nXi1Ci7US_70ImMlBk6X-6rOj81GFiUevaEpqc3wXIa9ArA4lxGwJqk9EeTrjlCPvyTTpG6QFf50NyRkrC-A")
    private String refreshToken;

}
