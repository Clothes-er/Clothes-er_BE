package com.yooyoung.clotheser.chat.controller;

import com.yooyoung.clotheser.chat.dto.ChatMessageRequest;
import com.yooyoung.clotheser.chat.dto.ChatMessageResponse;
import com.yooyoung.clotheser.chat.service.ChatService;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StompController {

    private final SimpMessageSendingOperations simpleMessageSendingOperations;
    private final ChatService chatService;

    // 새로운 사용자가 웹 소켓을 연결할 때 실행됨
    // @EventListener은 한 개의 매개변수만 가질 수 있다.
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        log.info("새로운 웹 소켓이 연결되었습니다.");
    }

    // 사용자가 웹 소켓 연결을 끊으면 실행됨
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccesor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccesor.getSessionId();

        log.info("세션 ID 끊김 : {}", sessionId);
    }

    // /pub/chats로 메시지 발행
    @MessageMapping("/chats/{roomId}")
    public void sendMessage(@DestinationVariable("roomId") Long roomId,
                            ChatMessageRequest chatMessageRequest,
                            Authentication authentication) throws BaseException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // 채팅 메시지 DB에 저장
        ChatMessageResponse chatMessageResponse = chatService.createChatMessage(chatMessageRequest, roomId, userDetails.user);

        // /sub/message를 구독 중인 client에 메세지 보내기
        simpleMessageSendingOperations.convertAndSend("/sub/chats/" + roomId, chatMessageResponse);
    }

}
