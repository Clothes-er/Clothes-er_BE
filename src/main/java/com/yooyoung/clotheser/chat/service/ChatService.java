package com.yooyoung.clotheser.chat.service;

import com.yooyoung.clotheser.chat.domain.ChatMessage;
import com.yooyoung.clotheser.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    // 채팅 메시지 저장
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

}
