package com.yooyoung.clotheser.chat.service;

import com.yooyoung.clotheser.chat.domain.ChatMessage;
import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.chat.dto.ChatRoomResponse;
import com.yooyoung.clotheser.chat.repository.ChatMessageRepository;
import com.yooyoung.clotheser.chat.repository.ChatRoomRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.domain.RentalImg;
import com.yooyoung.clotheser.rental.repository.RentalImgRepository;
import com.yooyoung.clotheser.rental.repository.RentalPriceRepository;
import com.yooyoung.clotheser.rental.repository.RentalRepository;
import com.yooyoung.clotheser.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    private final RentalRepository rentalRepository;
    private final RentalImgRepository rentalImgRepository;
    private final RentalPriceRepository rentalPriceRepository;

    // 채팅방 생성
    public ChatRoomResponse createChatRoom(Long rentalId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 대여글 존재 확인
        Rental rental = rentalRepository.findByIdAndDeletedAtNull(rentalId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_RENTAL, NOT_FOUND));

        // 대여글 작성자가 아닌지 확인
        if (rental.getUser().getId().equals(user.getId())) {
            throw new BaseException(FORBIDDEN_CREATE_CHAT_ROOM, FORBIDDEN);
        }

        // 채팅방이 이미 존재하는지 확인 (대여자가 동일한 대여글에서 채팅방 하나만 가능)
        if (chatRoomRepository.existsChatRoomByBuyerIdAndRentalId(user.getId(), rentalId)) {
            throw new BaseException(CHAT_ROOM_EXISTS, CONFLICT);
        }

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .buyer(user)
                .lender(rental.getUser())
                .rental(rental)
                .build();
        chatRoomRepository.save(chatRoom);

        // 첫 번째 이미지 불러오기
        Optional<RentalImg> optionalImg = rentalImgRepository.findOneByRentalId(rental.getId());
        String rentalImgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

        // 가격 정보 중에 제일 싼 가격 불러오기
        Optional<Integer> optionalPrice = rentalPriceRepository.findMinPrice(rental);
        int minPrice = optionalPrice.orElse(0);

        return new ChatRoomResponse(chatRoom, chatRoom.getLender().getNickname(), rental, rentalImgUrl, minPrice);

    }

    // 채팅 메시지 저장
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

}
