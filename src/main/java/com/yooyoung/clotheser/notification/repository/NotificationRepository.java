package com.yooyoung.clotheser.notification.repository;

import com.yooyoung.clotheser.notification.domain.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<PushNotification, Long> {
    // 안 읽은 알림이 있는지 확인
    boolean existsByUserIdAndIsReadFalse(Long userId);

    // 알림 목록 조회
    List<PushNotification> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // 안 읽은 알림 개수
    int countByUserIdAndIsReadFalse(Long userId);
}
