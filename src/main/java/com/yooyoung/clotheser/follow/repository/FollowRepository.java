package com.yooyoung.clotheser.follow.repository;

import com.yooyoung.clotheser.follow.domain.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFolloweeIdAndDeletedAtNull(Long followerId, Long followeeId);
}
