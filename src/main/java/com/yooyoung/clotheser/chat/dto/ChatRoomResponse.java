package com.yooyoung.clotheser.chat.dto;

import com.yooyoung.clotheser.rental.domain.RentalState;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
// 채팅방 내역 (채팅 화면)
public class ChatRoomResponse {

    private Long id;
    private Long buyer;
    private Long lender;

    // 대여글 정보
    private Long rentalId;
    private String imgUrl;
    private String title;
    private int minPrice;

    private RentalState rentalState;

    // 채팅 메시지 목록
    private List<ChatMessageResponse> messages;

}
