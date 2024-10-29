package com.yooyoung.clotheser.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class NotificationListResponse {
    @Schema(title = "안 읽은 알림 개수", example = "3")
    private int countOfNotReadNotifications;
    private List<NotificationResponse> notificationList;

    public NotificationListResponse(int countOfNotReadNotification, List<NotificationResponse> notificationResponseList) {
        this.countOfNotReadNotifications = countOfNotReadNotification;
        this.notificationList = notificationResponseList;
    }
}
