package com.yooyoung.clotheser.chat.controller;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.chat.dto.ChatMessageRequest;
import com.yooyoung.clotheser.chat.dto.ChatMessageResponse;
import com.yooyoung.clotheser.chat.repository.ChatRoomRepository;
import com.yooyoung.clotheser.chat.service.RentalChatService;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.notification.domain.NotificationType;
import com.yooyoung.clotheser.notification.dto.NotificationRequest;
import com.yooyoung.clotheser.notification.service.NotificationService;
import com.yooyoung.clotheser.user.domain.CustomUserDetails;
import com.yooyoung.clotheser.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
public class StompController {
    private final SimpMessageSendingOperations simpleMessageSendingOperations;
    private final SubProtocolWebSocketHandler subProtocolWebSocketHandler;
    private final RentalChatService rentalChatService;
    private final NotificationService notificationService;
    private final ChatRoomRepository chatRoomRepository;

    public StompController(SimpMessageSendingOperations simpleMessageSendingOperations,
                           WebSocketHandler webSocketHandler,
                           RentalChatService rentalChatService,
                           NotificationService notificationService,
                           ChatRoomRepository chatRoomRepository) {
        this.simpleMessageSendingOperations = simpleMessageSendingOperations;
        this.subProtocolWebSocketHandler = (SubProtocolWebSocketHandler) webSocketHandler;
        this.rentalChatService = rentalChatService;
        this.notificationService = notificationService;
        this.chatRoomRepository = chatRoomRepository;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        log.info("새로운 웹 소켓이 연결되었습니다.");

    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccesor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccesor.getSessionId();

        log.info("세션 ID 끊김 : {}", sessionId);
    }

    // /pub/chats로 메시지 발행
    @Transactional
    @MessageMapping("/chats/{roomId}")
    public void sendMessage(@DestinationVariable("roomId") Long roomId,
                            ChatMessageRequest chatMessageRequest,
                            Authentication authentication) {
        try {
            // 헤더에 토큰이 없거나 잘못된 경우
            if (authentication == null) {
                throw new BaseException(FORBIDDEN_ACCESS_JWT, UNAUTHORIZED);
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.user;

            // 메시지 입력 확인
            if (chatMessageRequest == null || chatMessageRequest.getMessage() == null
                    || chatMessageRequest.getMessage().isBlank()) {
                throw new BaseException(REQUEST_EMPTY_MESSAGE, BAD_REQUEST);
            }

            // 채팅방 존재 확인
            ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new BaseException(NOT_FOUND_CHAT_ROOM, NOT_FOUND));

            // 채팅 메시지 DB에 저장
            ChatMessageResponse chatMessageResponse = rentalChatService.createChatMessage(chatMessageRequest, chatRoom, user);

            sendWebSocketMessage(roomId, chatMessageResponse);

            // 상대방이 접속해있지 않다면 푸시 알림
            if (isNotConnectedToOther()) {
                sendFCMNotification(chatRoom, user, chatMessageResponse.getMessage());
            }
        }
        // TODO: 오류 처리 정교화 + 웹소켓 연결 끊기
        catch (BaseException exception) {
            log.error(exception.getStatus().getMessage());
            simpleMessageSendingOperations.convertAndSend("/sub/chats/" + roomId,
                    new ResponseEntity<>(new BaseResponse<>(exception.getStatus()), exception.getHttpStatus()));
        }
    }

    private void sendWebSocketMessage(Long roomId, ChatMessageResponse chatMessageResponse) {
        simpleMessageSendingOperations.convertAndSend("/sub/chats/" + roomId,
                new ResponseEntity<>(new BaseResponse<>(chatMessageResponse), CREATED));
    }

    private boolean isNotConnectedToOther() {
        return subProtocolWebSocketHandler.getStats().getWebSocketSessions() == 1;
    }

    @Async
    protected void sendFCMNotification(ChatRoom chatRoom, User user, String message) throws BaseException {
        NotificationRequest notificationRequest = createNotificationRequest(chatRoom, user, message);
        notificationService.sendNotification(notificationRequest);
    }

    private NotificationRequest createNotificationRequest(ChatRoom chatRoom, User user, String message) {
        User opponent;
        if (chatRoom.getBuyer().getId().equals(user.getId())) {
            opponent = chatRoom.getLender();
        }
        else {
            opponent = chatRoom.getBuyer();
        }

        return NotificationRequest.builder()
                .user(opponent)
                .type(chatRoom.getRental() != null ? NotificationType.RENTAL_CHAT : NotificationType.USER_CHAT)
                .image(user.getProfileUrl())
                .sourceId(chatRoom.getId())
                .title(user.getNickname())
                .content(message)
                .build();
    }
}
