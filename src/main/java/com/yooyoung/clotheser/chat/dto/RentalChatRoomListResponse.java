package com.yooyoung.clotheser.chat.dto;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.global.entity.Time;
import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.rental.domain.RentalState;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
/* 채팅방 목록 */
public class RentalChatRoomListResponse {

    @Schema(title = "채팅방 id", example = "1")
    private Long id;

    // 상대방 정보
    @Schema(title = "암호화된 상대방 id", example = "xfweriok12")
    private String userSid;
    @Schema(title = "상대방 닉네임", example = "황숙명")
    private String nickname;
    @Schema(title = "상대방 프로필 사진 URL", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/profiles/noonsong.png")
    private String profileImgUrl;

    // 대여글 정보
    @Schema(title = "대여글 제목", example = "스퀘어 블라우스")
    private String title;
    @Schema(title = "대여글 썸네일 (첫 번째 사진)", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/rentals/7c8adc06-7705-4323-90f2-3e2571eebbca_summer_nasi_3.jpg")
    private String rentalImgUrl;
    @Schema(title = "대여글 삭제 여부", example = "false")
    private Boolean isDeleted;

    @Schema(title = "대여 상태", example = "RENTED")
    private RentalState rentalState;

    @Schema(title = "최근 메시지", example = "안녕하세요~")
    private String recentMessage;
    @Schema(title = "마지막으로 메시지 보낸 시간", example = "3시간 전")
    private String recentMessageTime;

    public RentalChatRoomListResponse(ChatRoom chatRoom, String userSid, RentalInfo rentalInfo,
                                      String recentMessage, String rentalImgUrl, User opponent) {
        this.id = chatRoom.getId();

        this.userSid = userSid;
        this.nickname = opponent.getNickname();
        this.profileImgUrl = opponent.getProfileUrl();

        this.title = chatRoom.getRental().getTitle();
        this.rentalImgUrl = rentalImgUrl;
        this.isDeleted = chatRoom.getRental().getDeletedAt() != null;

        if (rentalInfo != null) {
            this.rentalState = rentalInfo.getState();
        }

        this.recentMessage = recentMessage;
        this.recentMessageTime = Time.calculateTime(chatRoom.getUpdatedAt());

    }

}
