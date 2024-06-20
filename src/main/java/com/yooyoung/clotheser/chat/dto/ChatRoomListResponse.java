package com.yooyoung.clotheser.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.global.entity.Time;
import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.rental.domain.RentalState;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Setter(AccessLevel.NONE)
/* 채팅방 목록 */
public class ChatRoomListResponse {

    @Schema(title = "채팅방 id", example = "1")
    private Long id;

    // 상대방 정보
    @Schema(title = "상대방 닉네임", example = "황숙명")
    private String nickname;
    @Schema(title = "상대방 프로필 사진 URL", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/profiles/noonsong.png")
    private String profileImgUrl;

    // 대여글 정보
    @Schema(title = "대여글 제목", example = "스퀘어 블라우스")
    private String title;
    @Schema(title = "대여글 썸네일 (첫 번째 사진)", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/rentals/7c8adc06-7705-4323-90f2-3e2571eebbca_summer_nasi_3.jpg")
    private String rentalImgUrl;

    @Schema(title = "대여 상태", example = "RENTED")
    private RentalState rentalState;

    @Schema(title = "대여 시작일", example = "2024년 06월 20일")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일", timezone = "Asia/Seoul")
    private LocalDate startDate;

    @Schema(title = "반납 예정일", example = "2024년 06월 25일")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일", timezone = "Asia/Seoul")
    private LocalDate endDate;

    @Schema(title = "최근 메시지", example = "안녕하세요~")
    private String recentMessage;
    @Schema(title = "마지막으로 메시지 보낸 시간", example = "2024년 06월 20일 17:31:29")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm:ss", timezone = "Asia/Seoul")
    private String recentMessageTime;

    public ChatRoomListResponse(ChatRoom chatRoom, RentalInfo rentalInfo, String recentMessage, String rentalImgUrl, User opponent) {
        this.id = chatRoom.getId();

        this.nickname = opponent.getNickname();
        this.profileImgUrl = opponent.getProfileUrl();

        this.title = chatRoom.getRental().getTitle();
        this.rentalImgUrl = rentalImgUrl;

        if (rentalInfo != null) {
            this.rentalState = rentalInfo.getState();
            this.startDate = rentalInfo.getStartDate();
            this.endDate = rentalInfo.getEndDate();
        }

        this.recentMessage = recentMessage;
        this.recentMessageTime = Time.calculateTime(chatRoom.getUpdatedAt());


    }

}
