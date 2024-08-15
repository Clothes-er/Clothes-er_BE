package com.yooyoung.clotheser.chat.dto;

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

    @Schema(description = "채팅 메시지 목록", type = "array")
    private List<ChatMessageResponse> messages;

    /* 유저 채팅방 생성 시 사용 */
    public UserChatRoomResponse(Long id, String opponentSid, String opponentNickname) {
        this.id = id;
        this.opponentSid = opponentSid;
        this.opponentNickname = opponentNickname;
    }

    /* 유저 채팅방 조회 시 사용 */
    public UserChatRoomResponse(Long id, String opponentSid, String opponentNickname, List<ChatMessageResponse> messages) {
        this.id = id;
        this.opponentSid = opponentSid;
        this.opponentNickname = opponentNickname;
        this.messages = messages;
    }
}
