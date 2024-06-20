package com.yooyoung.clotheser.user.dto;

import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Setter(AccessLevel.NONE)
public class AddressResponse {

    @Schema(title = "이메일", example = "noonsong@gmail.com")
    private String email;

    @Schema(title = "위도", example = "37.602354")
    private double latitude;

    @Schema(title = "경도", example = "127.026905")
    private double longitude;

    public AddressResponse(User user) {
        this.email = user.getEmail();
        this.latitude = user.getLatitude();
        this.longitude = user.getLongitude();
    }

}
