package com.yooyoung.clotheser.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class UserProfileResponse {
    private ProfileResponse profile;

    @Schema(title = "팔로잉 여부", example = "false")
    private Boolean isFollowing;

    public UserProfileResponse(ProfileResponse profile, Boolean isFollowing) {
        this.profile = profile;
        this.isFollowing = isFollowing;
    }
}
