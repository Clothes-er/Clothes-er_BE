package com.yooyoung.clotheser.chat.repository;

import com.yooyoung.clotheser.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Optional<ChatMessage> findFirstByRoomIdOrderByCreatedAtDesc(Long roomId);

    List<ChatMessage> findAllByRoomId(Long roomId);
}
