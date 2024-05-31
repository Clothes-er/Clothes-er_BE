package com.yooyoung.clotheser.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StompController {

    private final SimpMessageSendingOperations simpleMessageSendingOperations;

    // 새로운 사용자가 웹 소켓을 연결할 때 실행됨
    // @EventListener은 한 개의 매개변수만 가질 수 있다.
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        log.info("Received a new web socket connection");
    }

    // 사용자가 웹 소켓 연결을 끊으면 실행됨
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccesor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccesor.getSessionId();

        log.info("sessionId Disconnected : {}", sessionId);
    }

    // / pub/message로 메시지 발행
    @MessageMapping("/message")
    @SendTo("/sub/message")
    public void sendMessage(Map<String, Object> params) {
        // /sub/message 에 구독 중인 client에 메세지를 보내기
        simpleMessageSendingOperations.convertAndSend("/sub/message/" + params.get("channelId"), params);
    }

}
