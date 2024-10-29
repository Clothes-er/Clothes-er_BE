package com.yooyoung.clotheser.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.notification.domain.NotificationType;
import com.yooyoung.clotheser.notification.dto.DeviceTokenRequest;
import com.yooyoung.clotheser.notification.dto.NotificationRequest;
import com.yooyoung.clotheser.notification.repository.NotificationRepository;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public BaseResponseStatus saveDeviceToken(User user, DeviceTokenRequest deviceTokenRequest) throws BaseException {
        String deviceToken = deviceTokenRequest.getDeviceToken();
        user.updateDeviceToken(deviceToken);
        userRepository.save(user);

        return SUCCESS;
    }

    public void sendNotification(NotificationRequest notificationRequest) throws BaseException {
        String token = notificationRequest.getUser().getDeviceToken();
        if (token == null) {
            return;
        }

        Message message = createNotification(notificationRequest, token);
        try {
            FirebaseMessaging.getInstance().send(message);
            if (isNotChat(notificationRequest.getType())) {
                saveNotification(notificationRequest);
            }
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
                .putData("sourceId", String.valueOf(notificationRequest.getSourceId()))
                .build();
    }

    private boolean isNotChat(NotificationType type) {
        return type != NotificationType.RENTAL_CHAT && type != NotificationType.USER_CHAT;
    }

    private void saveNotification(NotificationRequest notificationRequest) {
        notificationRepository.save(notificationRequest.toEntity());
    }
}
