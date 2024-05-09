package com.yooyoung.clotheser.user.repository;

import com.yooyoung.clotheser.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByNicknameAndDeletedAtNull(String nickname);

    boolean existsByEmailAndDeletedAtNull(String email);

    boolean existsByPhoneNumberAndDeletedAtNull(String phoneNumber);
}
