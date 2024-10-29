package com.yooyoung.clotheser.notification.dto;

import com.yooyoung.clotheser.notification.domain.NotificationType;
import com.yooyoung.clotheser.notification.domain.PushNotification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class NotificationResponse {
    @Schema(title = "알림 id", example = "1")
    private Long id;

    @Schema(title = "알림 이미지", example = "null / 상대방 프로필 사진")
    private String image;

    @Schema(title = "알림 제목", example = "신고 / 팔로우")
    private String title;

    @Schema(title = "알림 내용", example = "신고 내용 검토 결과 신고가 반려되었습니다.")
    private String content;

    @Schema(title = "알림 종류")
    private NotificationType type;

    @Schema(title = "알림 출처 id", example = "1", description = "null / 팔로워 id")
    private Long sourceId;

    @Schema(title = "알림 확인 여부", example = "false")
    private Boolean isRead;

    public NotificationResponse(PushNotification notification, String image) {
        this.id = notification.getId();
        this.image = image;
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.type = notification.getType();
        this.sourceId = notification.getSourceId();
        this.isRead = notification.getIsRead();
    }
}
