package com.yooyoung.clotheser.clothes.domain;

import com.yooyoung.clotheser.clothes.dto.ClothesRequest;
import com.yooyoung.clotheser.user.domain.Gender;
import com.yooyoung.clotheser.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Builder
@Getter
@DynamicUpdate
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Clothes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private Long rentalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(columnDefinition = "TINYINT(1)")
    private Gender gender;

    @Column(length = 10)
    private String category;

    @Column(length = 20)
    private String style;

    private Integer price;

    @Column(length = 20)
    private String brand;

    @Column(length = 10)
    private String size;

    private String shoppingUrl;

    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    @ColumnDefault("true")
    private Boolean isPublic;

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

    // 대여글 연결
    public Clothes updateRental(Long rentalId) {
        this.rentalId = rentalId;
        return this;
    }

    // 보유 옷 수정
    public Clothes updateClothes(ClothesRequest clothesRequest, User user) {
        this.rentalId = clothesRequest.getRentalId();
        this.user = user;
        this.name = clothesRequest.getName();
        this.description = clothesRequest.getDescription();
        this.gender = clothesRequest.getGender();
        this.category = clothesRequest.getCategory();
        this.style = clothesRequest.getStyle();
        this.price = clothesRequest.getPrice();
        this.brand = clothesRequest.getBrand();
        this.size = clothesRequest.getSize();
        this.shoppingUrl = clothesRequest.getShoppingUrl();
        this.isPublic = clothesRequest.getIsPublic();
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    // 보유 옷 삭제
    public Clothes deleteClothes() {
        this.deletedAt = LocalDateTime.now();
        return this;
    }

}
