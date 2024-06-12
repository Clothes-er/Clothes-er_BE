package com.yooyoung.clotheser.user.dto;

import com.yooyoung.clotheser.user.domain.User;
import lombok.*;

@Data
@Setter(AccessLevel.NONE)
public class AddressResponse {

    private String email;
    private double latitude;
    private double longitude;

    public AddressResponse(User user) {
        this.email = user.getEmail();
        this.latitude = user.getLatitude();
        this.longitude = user.getLongitude();
    }

}
