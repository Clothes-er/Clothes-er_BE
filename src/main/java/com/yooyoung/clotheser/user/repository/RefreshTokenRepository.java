package com.yooyoung.clotheser.user.repository;

import com.yooyoung.clotheser.user.domain.RefreshToken;
import com.yooyoung.clotheser.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    RefreshToken findByUserId(Long userId);

}
