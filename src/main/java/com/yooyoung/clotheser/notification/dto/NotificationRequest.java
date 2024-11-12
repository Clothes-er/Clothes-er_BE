package com.yooyoung.clotheser.notification.dto;

import com.yooyoung.clotheser.notification.domain.PushNotification;
import com.yooyoung.clotheser.notification.domain.NotificationType;
import com.yooyoung.clotheser.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Builder
@Data
@Setter(AccessLevel.NONE)
public class NotificationRequest {
    private User user;
    private NotificationType type;
    private String image;
    private String sourceId;
    private String title;
    private String content;

    public PushNotification toEntity() {
        return PushNotification.builder()
                .user(user)
                .type(type)
                .sourceId(sourceId)
                .title(title)
                .content(content)
                .build();
    }
}
