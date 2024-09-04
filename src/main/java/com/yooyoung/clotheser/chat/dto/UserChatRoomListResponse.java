package com.yooyoung.clotheser.chat.dto;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.global.entity.Time;
import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class UserChatRoomListResponse {

    @Schema(title = "채팅방 id", example = "1")
    private Long id;

    // 상대방 정보
    @Schema(title = "암호화된 상대방 id", example = "M0h1QXdzUlVzNkRwckdUeUEvbjVQZz09")
    private String userSid;
    @Schema(title = "상대방 닉네임", example = "황숙명")
    private String nickname;
    @Schema(title = "상대방 프로필 사진 URL", example = "https://clotheser-s3-bucket.s3.ap-northeast-2.amazonaws.com/profiles/noonsong.png")
    private String profileImgUrl;
    @Schema(title = "상대방 유예 여부", example = "false")
    private Boolean isSuspended;
    @Schema(title = "상대방 이용 제한 여부", example = "false")
    private Boolean isRestricted;

    @Schema(title = "최근 메시지", example = "안녕하세요~")
    private String recentMessage;
    @Schema(title = "마지막으로 메시지 보낸 시간", example = "3시간 전")
    private String recentMessageTime;

    public UserChatRoomListResponse(ChatRoom chatRoom, String userSid, String recentMessage, User opponent) {
        this.id = chatRoom.getId();
        this.userSid = userSid;
        this.nickname = opponent.getNickname();
        this.profileImgUrl = opponent.getProfileUrl();
        this.isSuspended = opponent.getIsSuspended();
        this.isRestricted = opponent.getIsRestricted();
        this.recentMessage = recentMessage;
        this.recentMessageTime = Time.calculateTime(chatRoom.getUpdatedAt());
    }

}
