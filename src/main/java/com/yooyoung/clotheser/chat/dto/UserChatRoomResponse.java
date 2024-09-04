package com.yooyoung.clotheser.chat.dto;

import com.yooyoung.clotheser.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.NONE)
public class UserChatRoomResponse {

    @Schema(title = "채팅방 id", example = "1")
    private Long id;

    @Schema(title = "암호화된 상대방 id", example = "M0h1QXdzUlVzNkRwckdUeUEvbjVQZz09")
    private String opponentSid;

    @Schema(title = "상대방 닉네임", example = "황숙명")
    private String opponentNickname;

    @Schema(title = "상대방 유예 여부", example = "false")
    private Boolean isSuspended;

    @Schema(title = "상대방 이용 제한 여부", example = "false")
    private Boolean isRestricted;

    @Schema(description = "채팅 메시지 목록", type = "array")
    private List<ChatMessageResponse> messages;

    /* 유저 채팅방 생성 시 사용 */
    public UserChatRoomResponse(Long id, String opponentSid, String opponentNickname) {
        this.id = id;
        this.opponentSid = opponentSid;
        this.opponentNickname = opponentNickname;
        this.isSuspended = false;
        this.isRestricted = false;
    }

    /* 유저 채팅방 조회 시 사용 */
    public UserChatRoomResponse(Long id, String opponentSid, User opponent, List<ChatMessageResponse> messages) {
        this.id = id;
        this.opponentSid = opponentSid;
        this.opponentNickname = opponent.getNickname();
        this.isSuspended = opponent.getIsSuspended();
        this.isRestricted = opponent.getIsRestricted();
        this.messages = messages;
    }
}
