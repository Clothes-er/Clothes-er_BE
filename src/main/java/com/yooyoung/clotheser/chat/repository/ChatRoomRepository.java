package com.yooyoung.clotheser.chat.repository;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsChatRoomByBuyerIdAndRentalId(Long buyerId, Long rentalId);

    // updatedAt이 null이 아닌 채팅방들만 보여주기 (null이면 채팅 메시지 없는 채팅방)
    @Query("select c from ChatRoom c where (c.buyer.id = :userId or c.lender.id = :userId) and c.updatedAt is not null order by c.updatedAt desc")
    List<ChatRoom> findAllByUserId(Long userId);

}
