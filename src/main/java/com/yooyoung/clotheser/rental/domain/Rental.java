package com.yooyoung.clotheser.rental.domain;

import com.yooyoung.clotheser.rental.dto.RentalRequest;
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
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    // TODO: 필요 시 보유 옷 FK 걸기
    private Long clothesId;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(columnDefinition = "TINYINT(1)")
    private Gender gender;

    @Column(length = 10)
    private String category;

    @Column(length = 20)
    private String style;

    @Column(length = 20)
    private String brand;

    @Column(length = 10)
    private String size;

    @Column(length = 10)
    private String fit;

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

    public Rental updateRental(RentalRequest rentalRequest, User user) {
        this.user = user;
        this.title = rentalRequest.getTitle();
        this.description = rentalRequest.getDescription();
        this.gender = rentalRequest.getGender();
        this.category = rentalRequest.getCategory();
        this.style = rentalRequest.getStyle();
        this.brand = rentalRequest.getBrand();
        this.size = rentalRequest.getSize();
        this.fit = rentalRequest.getFit();
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public Rental deleteRental() {
        this.deletedAt = LocalDateTime.now();
        return this;
    }

}
