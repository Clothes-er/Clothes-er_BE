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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.NOT_FOUND_DEVICE_TOKEN;
import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.SUCCESS;

@Slf4j
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
        Message message = createNotification(notificationRequest);

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM send - {}", response);
            if (isNotChat(notificationRequest.getType())) {
                saveNotification(notificationRequest);
            }
        } catch (FirebaseMessagingException e) {
            log.info("FCM except- {}", e.getMessage());
        }
    }

    private Message createNotification(NotificationRequest notificationRequest) throws BaseException {
        String token = notificationRequest.getUser().getDeviceToken();
        if (token == null) {
            throw new BaseException(NOT_FOUND_DEVICE_TOKEN, HttpStatus.NOT_FOUND);
        }

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
                .putData("sourceId", notificationRequest.getSourceId().toString())
                .build();
    }

    private boolean isNotChat(NotificationType type) {
        return type != NotificationType.RENTAL_CHAT && type != NotificationType.USER_CHAT;
    }

    private void saveNotification(NotificationRequest notificationRequest) {
        notificationRepository.save(notificationRequest.toEntity());
    }
}
