package com.yooyoung.clotheser.rental.domain;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentalCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ChatRoom room;

    @Column(nullable = false)
    private String clothesCheck;

}
