package com.yooyoung.clotheser.chat.repository;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsChatRoomByBuyerIdAndRentalId(Long buyerId, Long rentalId);

}
