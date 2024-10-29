package com.yooyoung.clotheser.notification.repository;

import com.yooyoung.clotheser.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 안 읽은 알림이 있는지 확인
    boolean existsByUserIdAndIsReadFalse(Long userId);
}
