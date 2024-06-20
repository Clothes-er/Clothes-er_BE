package com.yooyoung.clotheser.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
public class TokenResponse {

    @Schema(title = "JWT 인증 타입", defaultValue = "Bearer", example = "Bearer")
    private String grantType;

    @Schema(title = "액세스 토큰", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJub29uc29uZyIsImF1dGgiOiJST0xFX1VTRVIiLCJleHAiOjE3MTg4NjMwMDZ9.L6XMliUhdPZnv6H5wZN6SNrhYbnk2fEOSXH6V-xzrNagbL2nypiCdxw6chhmxRgXx7_5DJRIE1Txo0XyDD2Xng")
    private String accessToken;

    @Schema(title = "리프레시 토큰", example = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE3MjEzNjg2MDZ9.25nXi1Ci7US_70ImMlBk6X-6rOj81GFiUevaEpqc3wXIa9ArA4lxGwJqk9EeTrjlCPvyTTpG6QFf50NyRkrC-A")
    private String refreshToken;

}
