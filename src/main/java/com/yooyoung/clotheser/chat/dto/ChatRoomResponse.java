package com.yooyoung.clotheser.chat.dto;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.rental.domain.Rental;
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
    private String buyerNickname;
    private String lenderNickname;
    private String opponentNickname;

    // 대여글 정보
    private Long rentalId;
    private String rentalImgUrl;
    private String title;
    private Integer minPrice;

    // 옷 상태 체크 여부
    private Boolean isChecked;
    // 대여 상태
    private RentalState rentalState;

    // 채팅 메시지 목록
    private List<ChatMessageResponse> messages;

    /* 채팅방 생성 시 쓰는 생성자 */
    public ChatRoomResponse(ChatRoom chatRoom, String opponentNickname,
                            Rental rental, String rentalImgUrl, Integer minPrice) {
        this.id = chatRoom.getId();
        this.buyerNickname = chatRoom.getBuyer().getNickname();
        this.lenderNickname = chatRoom.getLender().getNickname();
        this.opponentNickname = opponentNickname;

        this.rentalId = rental.getId();
        this.rentalImgUrl = rentalImgUrl;
        this.title = rental.getTitle();
        this.minPrice = minPrice;
    }

    /* 채팅방 조회 시 쓰는 생성자 */
    public ChatRoomResponse(ChatRoom chatRoom, String opponentNickname, List<ChatMessageResponse> messages,
                            String rentalImgUrl, Integer minPrice, Boolean isChecked, RentalState rentalState) {
        this.id = chatRoom.getId();
        this.buyerNickname = chatRoom.getBuyer().getNickname();
        this.lenderNickname = chatRoom.getLender().getNickname();
        this.opponentNickname = opponentNickname;

        this.rentalId = chatRoom.getRental().getId();
        this.rentalImgUrl = rentalImgUrl;
        this.title = chatRoom.getRental().getTitle();
        this.minPrice = minPrice;

        this.isChecked = isChecked;
        this.rentalState = rentalState;
        this.messages = messages;
    }

}
