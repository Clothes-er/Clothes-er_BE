package com.yooyoung.clotheser.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class HomeNotificationResponse {
    @Schema(title = "알림 확인 여부", example = "true")
    private Boolean isRead;

    public HomeNotificationResponse(Boolean isRead) {
        this.isRead = isRead;
    }
}
