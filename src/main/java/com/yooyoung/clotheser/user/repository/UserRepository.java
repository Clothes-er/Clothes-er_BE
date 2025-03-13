package com.yooyoung.clotheser.user.repository;

import com.yooyoung.clotheser.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByNicknameAndDeletedAtNull(String nickname);

    boolean existsByEmailAndDeletedAtNull(String email);

    boolean existsByPhoneNumberAndDeletedAtNull(String phoneNumber);

    Optional<User> findByIdAndDeletedAtNull(Long id);

    Optional<User> findByEmailAndDeletedAtNull(String email);

    // 회원 목록 조회
    List<User> findAllByDeletedAtNullOrderByCreatedAtDesc();

    // 회원 목록 검색
    @Query("SELECT u FROM User u " +
            "WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "AND u.deletedAt IS NULL " +
            "ORDER BY u.createdAt DESC")
    List<User> searchAll(String search);

}
