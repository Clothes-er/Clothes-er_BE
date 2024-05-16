package com.yooyoung.clotheser.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 1000)
    private String token;

    public RefreshToken updateRefreshToken(String token) {
        this.token = token;
        return this;
    }

    public void destroyRefreshToken() {
        this.token = null;
    }

}