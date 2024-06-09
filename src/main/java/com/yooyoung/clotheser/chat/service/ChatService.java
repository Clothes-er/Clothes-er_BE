package com.yooyoung.clotheser.chat.service;

import com.yooyoung.clotheser.chat.domain.ChatMessage;
import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.chat.dto.ChatMessageRequest;
import com.yooyoung.clotheser.chat.dto.ChatMessageResponse;
import com.yooyoung.clotheser.chat.dto.ChatRoomListResponse;
import com.yooyoung.clotheser.chat.dto.ChatRoomResponse;
import com.yooyoung.clotheser.chat.repository.ChatMessageRepository;
import com.yooyoung.clotheser.chat.repository.ChatRoomRepository;

import com.yooyoung.clotheser.global.entity.BaseException;

import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.domain.RentalImg;
import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.rental.domain.RentalState;
import com.yooyoung.clotheser.rental.repository.RentalImgRepository;
import com.yooyoung.clotheser.rental.repository.RentalInfoRepository;
import com.yooyoung.clotheser.rental.repository.RentalPriceRepository;
import com.yooyoung.clotheser.rental.repository.RentalRepository;
import com.yooyoung.clotheser.user.domain.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    private final RentalRepository rentalRepository;
    private final RentalImgRepository rentalImgRepository;
    private final RentalPriceRepository rentalPriceRepository;
    private final RentalInfoRepository rentalInfoRepository;

    /* 채팅방 생성 */
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
        Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(rental.getId());
        String rentalImgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

        // 가격 정보 중에 제일 싼 가격 불러오기
        Integer minPrice = rentalPriceRepository.findMinPrice(rental).orElse(null);

        return new ChatRoomResponse(chatRoom, chatRoom.getLender().getNickname(), rental, rentalImgUrl, minPrice);

    }

    /* 채팅방 목록 조회 */
    public List<ChatRoomListResponse> getChatRoomList(User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        List<ChatRoom> chatRoomList = chatRoomRepository.findAllByUserId(user.getId());
        List<ChatRoomListResponse> chatRoomResponseList = new ArrayList<>();
        for (ChatRoom chatRoom : chatRoomList) {

            // 로그인한 회원이 대여자인지 판매자인지 구분
            User opponent;
            if (chatRoom.getBuyer().getId().equals(user.getId())) {
                opponent = chatRoom.getLender();
            }
            else {
                opponent = chatRoom.getBuyer();
            }

            // 대여 상태 불러오기
            RentalInfo rentalInfo = rentalInfoRepository.findFirstByBuyerIdAndLenderIdAndRentalIdOrderByRentalTimeDesc(
                    chatRoom.getBuyer().getId(),
                    chatRoom.getLender().getId(),
                    chatRoom.getRental().getId()).orElse(null);

            // 채팅방의 최근 메시지 불러오기
            Optional<ChatMessage> optionalMessage = chatMessageRepository.findFirstByRoomIdOrderByCreatedAtDesc(chatRoom.getId());
            String recentMessage = optionalMessage.map(ChatMessage::getMessage).orElse(null);

            // 대여글의 첫 번째 이미지 불러오기
            Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(chatRoom.getRental().getId());
            String imgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

            chatRoomResponseList.add(new ChatRoomListResponse(chatRoom, rentalInfo, recentMessage, imgUrl, opponent));
        }
        return chatRoomResponseList;
    }

    /* 채팅 메시지 생성 후 DB에 저장 */
    public ChatMessageResponse createChatMessage(ChatMessageRequest chatMessageRequest, Long roomId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_CHAT_ROOM, NOT_FOUND));

        // 채팅방 참여자인지 확인
        if (!chatRoom.getBuyer().getId().equals(user.getId()) && !chatRoom.getLender().getId().equals(user.getId()) ) {
            throw new BaseException(FORBIDDEN_ENTER_CHAT_ROOM, FORBIDDEN);
        }

        ChatMessage chatMessage = chatMessageRequest.toEntity(user, chatRoom);
        // 채팅방 최근 활성화된 시간 변경
        chatRoomRepository.save(chatRoom.updateRecentMessageTime());
        // 채팅 메시지 저장
        return new ChatMessageResponse(chatMessageRepository.save(chatMessage));
    }

    /* 채팅방 조회 (채팅 메시지 목록 포함) */
    public ChatRoomResponse getChatRoom(Long roomId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_CHAT_ROOM, NOT_FOUND));

        // 채팅방 참여자인지 확인
        if (!chatRoom.getBuyer().getId().equals(user.getId()) && !chatRoom.getLender().getId().equals(user.getId()) ) {
            throw new BaseException(FORBIDDEN_ENTER_CHAT_ROOM, FORBIDDEN);
        }

        // 대화 내역 불러오기
        List<ChatMessage> chatMessageList = chatMessageRepository.findAllByRoomId(roomId);
        List<ChatMessageResponse> chatMessageResponseList = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessageList) {
            chatMessageResponseList.add(new ChatMessageResponse(chatMessage));
        }

        // 로그인한 회원이 대여자인지 판매자인지 구분
        User opponent;
        if (chatRoom.getBuyer().getId().equals(user.getId())) {
            opponent = chatRoom.getLender();
        }
        else {
            opponent = chatRoom.getBuyer();
        }

        // 유저 기반 채팅방인지 확인
        if (chatRoom.getRental() == null) {
            return new ChatRoomResponse(chatRoom, opponent.getNickname(), chatMessageResponseList, null, null, null);
        }

        // 첫 번째 이미지 불러오기
        Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(chatRoom.getRental().getId());
        String rentalImgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

        // 가격 정보 중에 제일 싼 가격 불러오기
        Integer minPrice = rentalPriceRepository.findMinPrice(chatRoom.getRental()).orElse(null);

        // 최근 대여 정보에서 대여 상태 불러오기
        RentalInfo rentalInfo = rentalInfoRepository.findFirstByBuyerIdAndLenderIdAndRentalIdOrderByRentalTimeDesc(
                chatRoom.getBuyer().getId(),
                chatRoom.getLender().getId(),
                chatRoom.getRental().getId()).orElse(null);
        RentalState rentalState = rentalInfo == null ? null : rentalInfo.getState();

        return new ChatRoomResponse(chatRoom, opponent.getNickname(), chatMessageResponseList, rentalImgUrl, minPrice, rentalState);

    }
}
