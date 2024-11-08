package com.yooyoung.clotheser.follow.repository;

import com.yooyoung.clotheser.follow.domain.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFolloweeIdAndDeletedAtNull(Long followerId, Long followeeId);
    Optional<Follow> findOneByFollowerIdAndFolloweeIdAndDeletedAtNull(Long followerId, Long followeeId);
}
