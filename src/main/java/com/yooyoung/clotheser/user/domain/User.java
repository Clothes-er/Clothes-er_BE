package com.yooyoung.clotheser.user.domain;

import com.yooyoung.clotheser.user.dto.FirstLoginRequest;
import com.yooyoung.clotheser.user.dto.UserProfileRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@DynamicUpdate
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
        @Index(name = "idx_user_location", columnList = "latitude, longitude")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String profileUrl;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate birthday;

    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "TINYINT(1)")
    private Gender gender;

    // null = 사용자가 추가 안 함
    private Integer height;
    private Integer weight;
    private Integer shoeSize;

    @ColumnDefault("0")
    private int rentalCount;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    @ColumnDefault("true")     // DB 기본값 설정
    @Builder.Default            // 객체 생성 시 기본값 설정
    private Boolean isFirstLogin = true;

    // 관리자 계정은 DB에서 직접 변경
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    @ColumnDefault("false")     // DB 기본값 설정
    @Builder.Default            // 객체 생성 시 기본값 설정
    private Boolean isAdmin = false;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ColumnDefault("null")
    private LocalDateTime lastLoginAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ColumnDefault("null")
    private LocalDateTime updatedAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ColumnDefault("null")
    private LocalDateTime deletedAt;

    // 최초 로그인 시 회원 정보 수정
    public User firstLogin(FirstLoginRequest firstLoginRequest) {
        this.isFirstLogin = false;
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = this.lastLoginAt;

        this.latitude = firstLoginRequest.getLatitude();
        this.longitude = firstLoginRequest.getLongitude();

        this.gender = firstLoginRequest.getGender();
        this.height = firstLoginRequest.getHeight();
        this.weight = firstLoginRequest.getWeight();
        this.shoeSize = firstLoginRequest.getShoeSize();

        return this;
    }

    // 마지막으로 로그인한 시간 수정
    public User updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
        return this;
    }

    // 회원의 레벨 조회
    public int getUserLevel() {
        int rentalCount = this.getRentalCount();
        int level = 0;

        if (rentalCount >= 1 && rentalCount <= 3) {
            level = 1;
        }
        else if (rentalCount >= 4 && rentalCount <= 6) {
            level = 2;
        }
        else if (rentalCount >= 7 && rentalCount <= 10) {
            level = 3;
        }
        else if (rentalCount >= 11 && rentalCount <= 14) {
            level = 4;
        }
        else if (rentalCount >= 15) {
            level = 5;
        }

        return level;
    }

    // 대여 횟수 증가
    public User increaseRentalCount() {
        this.rentalCount++;
        return this;
    }

    // 주소 수정
    public User updateAddress(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    // 프로필 이미지 수정
    public User updateProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    // 스펙 수정
    public User updateSpec(UserProfileRequest userProfileRequest) {
        this.gender = userProfileRequest.getGender();
        this.height = userProfileRequest.getHeight();
        this.weight = userProfileRequest.getWeight();
        this.shoeSize = userProfileRequest.getShoeSize();
        this.updatedAt = LocalDateTime.now();
        return this;
    }

}
