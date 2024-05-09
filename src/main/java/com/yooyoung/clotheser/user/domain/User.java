package com.yooyoung.clotheser.user.domain;

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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String profileUrl;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate birthday;

    private double latitude;
    private double longitude;

    @Column(columnDefinition = "TINYINT(1)")
    private Boolean gender;

    private int height;
    private int weight;
    private int shoeSize;

    @ColumnDefault("0")
    private int level;

    @ColumnDefault("0")
    private int rentalCount;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    @ColumnDefault("false")     // DB 기본값 설정
    @Builder.Default            // 객체 생성 시 기본값 설정
    private Boolean isFirstLogin = false;

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
    private LocalDateTime updatedAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ColumnDefault("null")
    private LocalDateTime deletedAt;

}
