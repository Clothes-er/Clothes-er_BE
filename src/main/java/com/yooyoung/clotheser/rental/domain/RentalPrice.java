package com.yooyoung.clotheser.rental.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Builder
@Entity
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentalPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Rental rental;

    @Column(nullable = false)
    private int days;

    @Column(nullable = false)
    private int price;

}
