package com.yooyoung.clotheser.chat.dto;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.domain.RentalState;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
// 채팅방 내역 (채팅 화면)
public class ChatRoomResponse {

    @Schema(title = "채팅방 id", example = "1")
    private Long id;

    @Schema(title = "대여자 닉네임", example = "김눈송")
    private String buyerNickname;
    @Schema(title = "판매자 닉네임", example = "황숙명")
    private String lenderNickname;
    @Schema(title = "상대방 닉네임", example = "황숙명")
    private String opponentNickname;

    // 대여글 정보
    @Schema(title = "대여글 id", example = "1")
    private Long rentalId;
    @Schema(title = "대여글 썸네일 (첫 번째 사진)", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/rentals/7c8adc06-7705-4323-90f2-3e2571eebbca_summer_nasi_3.jpg")
    private String rentalImgUrl;
    @Schema(title = "대여글 제목", example = "스퀘어 블라우스")
    private String title;
    @Schema(title = "대여글 최소 가격", example = "1000")
    private Integer minPrice;

    @Schema(title = "옷 상태 체크 여부", example = "true")
    private Boolean isChecked;

    @Schema(title = "대여 상태", example = "RETURNED")
    private RentalState rentalState;

    @Schema(description = "채팅 메시지 목록", type = "array", example = "[\"대여 가능한가요?\", \"네 가능합니다.\"]")
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
