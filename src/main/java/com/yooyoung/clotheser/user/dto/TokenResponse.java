package com.yooyoung.clotheser.user.dto;

import lombok.*;

@Getter
@Builder
public class TokenResponse {

    private String grantType; // JWT에 대한 인증 타입 (Bearer)
    private String accessToken;
    private String refreshToken;

}
