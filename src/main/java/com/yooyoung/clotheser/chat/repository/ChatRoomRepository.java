package com.yooyoung.clotheser.chat.repository;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 대여글 채팅방 확인 (id)
    Optional<ChatRoom> findOneByIdAndRentalIdNotNull(Long id);

    // 대여글 채팅방 확인 (userId, rentalId)
    Optional<ChatRoom> findOneByBuyerIdAndRentalId(Long buyerId, Long rentalId);

    // 유저 채팅방 확인 (id)
    Optional<ChatRoom> findOneByIdAndRentalIdNull(Long id);

    // 유저 채팅방 확인 (userId)
    Optional<ChatRoom> findOneByBuyerIdAndLenderIdAndRentalIdNull(Long buyerId, Long lenderId);

    // updatedAt이 null이 아닌 대여글 채팅방 목록 조회 (null이면 채팅 메시지 없는 채팅방)
    @Query("select c from ChatRoom c where (c.buyer.id = :userId or c.lender.id = :userId) and " +
            "c.rental is not null and c.updatedAt is not null order by c.updatedAt desc")
    List<ChatRoom> findRentalChatRoomsByUserId(Long userId);

    // 유저 채팅방 목록 조회
    @Query("select c from ChatRoom c where (c.buyer.id = :userId or c.lender.id = :userId) and " +
            "c.rental is null and c.updatedAt is not null order by c.updatedAt desc")
    List<ChatRoom> findUserChatRoomsByUserId(Long userId);

}
