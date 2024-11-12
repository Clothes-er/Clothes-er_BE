package com.yooyoung.clotheser.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.notification.domain.NotificationType;
import com.yooyoung.clotheser.notification.domain.PushNotification;
import com.yooyoung.clotheser.notification.dto.*;
import com.yooyoung.clotheser.notification.repository.NotificationRepository;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final AESUtil aesUtil;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /* 디바이스 토큰 저장 */
    public BaseResponseStatus saveDeviceToken(User user, DeviceTokenRequest deviceTokenRequest) throws BaseException {
        String deviceToken = deviceTokenRequest.getDeviceToken();
        user.updateDeviceToken(deviceToken);
        userRepository.save(user);

        return SUCCESS;
    }

    /* Firebase 푸시 알림 요청 */
    public void sendNotification(NotificationRequest notificationRequest) throws BaseException {
        // 알림 목록에 저장
        if (isNotChat(notificationRequest.getType())) {
            saveNotification(notificationRequest);
        }

        // 디바이스 토큰이 없으면 알림만 저장
        String token = notificationRequest.getUser().getDeviceToken();
        if (token == null) {
            return;
        }

        // 푸시 알림 전송
        Message message = createNotification(notificationRequest, token);
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new BaseException(FCM_ERROR, INTERNAL_SERVER_ERROR);
        }
    }

    private Message createNotification(NotificationRequest notificationRequest, String token) {
        return Message.builder()
                .setToken(token)
                .setNotification(
                        Notification.builder()
                                .setTitle(notificationRequest.getTitle())
                                .setBody(notificationRequest.getContent())
                                .setImage(notificationRequest.getImage())
                                .build()
                )
                .putData("type", notificationRequest.getType().name())
                .putData("sourceId", notificationRequest.getSourceId())
                .build();
    }

    private boolean isNotChat(NotificationType type) {
        return type != NotificationType.RENTAL_CHAT && type != NotificationType.USER_CHAT;
    }

    private void saveNotification(NotificationRequest notificationRequest) {
        notificationRepository.save(notificationRequest.toEntity());
    }

    /* 홈 알림 확인 여부 조회 */
    public HomeNotificationResponse getHomeNotification(User user) {
        boolean isRead = !notificationRepository.existsByUserIdAndIsReadFalse(user.getId());
        return new HomeNotificationResponse(isRead);
    }

    /* 알림 목록 조회 */
    public NotificationListResponse getNotificationList(User user) throws BaseException {
        int countOfNotReadNotification = notificationRepository.countByUserIdAndIsReadFalse(user.getId());

        List<PushNotification> notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
        List<NotificationResponse> notificationResponseList = new ArrayList<>();
        for (PushNotification notification : notifications) {
            String image = getNotificationImage(notification);
            NotificationResponse response = new NotificationResponse(notification, image);
            notificationResponseList.add(response);
        }

        return new NotificationListResponse(countOfNotReadNotification, notificationResponseList);
    }

    private String getNotificationImage(PushNotification notification) throws BaseException {
        String image = null;
        if (notification.getType() == NotificationType.FOLLOW) {
            Long userId = aesUtil.decryptUserSid(notification.getSourceId());
            User opponent = userRepository.findByIdAndDeletedAtNull(userId)
                    .orElseThrow(() -> new BaseException(NOT_FOUND_USER, NOT_FOUND));
            image = opponent.getProfileUrl();
        }
        return image;
    }

    /* 알림 읽음 처리 */
    public BaseResponseStatus readNotification(User user, Long notificationId) throws BaseException {
        PushNotification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new BaseException(NOT_FOUND_NOTIFICATION, NOT_FOUND));

        if (!notification.getIsRead()) {
            notification.updateIsRead(true);
            notificationRepository.save(notification);
        }

        return SUCCESS;
    }
}
