package com.yooyoung.clotheser.chat.service;

import com.yooyoung.clotheser.chat.domain.ChatMessage;
import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.chat.dto.ChatMessageRequest;
import com.yooyoung.clotheser.chat.dto.ChatMessageResponse;
import com.yooyoung.clotheser.chat.dto.RentalChatRoomListResponse;
import com.yooyoung.clotheser.chat.dto.RentalChatRoomResponse;
import com.yooyoung.clotheser.chat.repository.ChatMessageRepository;
import com.yooyoung.clotheser.chat.repository.ChatRoomRepository;

import com.yooyoung.clotheser.global.entity.BaseException;

import com.yooyoung.clotheser.global.entity.ChatRoomException;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.rental.domain.*;
import com.yooyoung.clotheser.rental.repository.*;
import com.yooyoung.clotheser.review.repository.ReviewRepository;
import com.yooyoung.clotheser.user.domain.Role;
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
public class RentalChatService {

    private final AESUtil aesUtil;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    private final RentalRepository rentalRepository;
    private final RentalImgRepository rentalImgRepository;
    private final RentalPriceRepository rentalPriceRepository;
    private final RentalInfoRepository rentalInfoRepository;
    private final RentalCheckRepository rentalCheckRepository;

    private final ReviewRepository reviewRepository;

    /* 대여글 채팅방 생성 */
    public RentalChatRoomResponse createRentalChatRoom(Long rentalId, User user) throws BaseException, ChatRoomException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 유예된 회원 확인
        if (user.getIsSuspended()) {
            throw new BaseException(USE_RESTRICTED, FORBIDDEN);
        }

        // 대여글 존재 확인
        Rental rental = rentalRepository.findByIdAndDeletedAtNull(rentalId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_RENTAL, NOT_FOUND));

        // 대여글 작성자가 아닌지 확인
        if (rental.getUser().getId().equals(user.getId())) {
            throw new BaseException(FORBIDDEN_CREATE_CHAT_ROOM, FORBIDDEN);
        }

        // 채팅방이 이미 존재하는지 확인 (대여자가 동일한 대여글에서 채팅방 하나만 가능) -> roomId 리턴
        Optional<ChatRoom> existedChatRoom = chatRoomRepository.findOneByBuyerIdAndRentalId(user.getId(), rentalId);
        if (existedChatRoom.isPresent()) {
            throw new ChatRoomException(RENTAL_CHAT_ROOM_EXISTS, CONFLICT, existedChatRoom.get().getId());
        }

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .buyer(user)
                .lender(rental.getUser())
                .rental(rental)
                .build();
        chatRoomRepository.save(chatRoom);

        // 판매자의 id 암호화하기
        String lenderSid = aesUtil.encryptUserId(chatRoom.getLender().getId());

        // 첫 번째 이미지 불러오기
        Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(rental.getId());
        String rentalImgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

        // 가격 정보 중에 제일 싼 가격 및 일수 불러오기
        int minPrice = 0;
        int minDays = 0;
        Optional<RentalPrice> minRentalPrice = rentalPriceRepository.findFirstByRentalOrderByPriceAscDaysAsc(rental);
        if (minRentalPrice.isPresent()) {
            minPrice = minRentalPrice.get().getPrice();
            minDays = minRentalPrice.get().getDays();
        }

        return new RentalChatRoomResponse(chatRoom, lenderSid, chatRoom.getLender(), rental, rentalImgUrl, minPrice, minDays);

    }

    /* 대여글 채팅방 목록 조회 */
    public List<RentalChatRoomListResponse> getRentalChatRoomList(User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        List<ChatRoom> chatRoomList = chatRoomRepository.findRentalChatRoomsByUserId(user.getId());
        List<RentalChatRoomListResponse> chatRoomResponseList = new ArrayList<>();
        for (ChatRoom chatRoom : chatRoomList) {

            // 로그인한 회원이 대여자인지 판매자인지 구분
            User opponent;
            if (chatRoom.getBuyer().getId().equals(user.getId())) {
                opponent = chatRoom.getLender();
            }
            else {
                opponent = chatRoom.getBuyer();
            }

            // 상대방의 id 암호화하기
            String opponentSid = aesUtil.encryptUserId(opponent.getId());

            // 대여 상태 불러오기
            RentalInfo rentalInfo = rentalInfoRepository.findFirstByBuyerIdAndLenderIdAndRentalIdOrderByRentalTimeDesc(
                    chatRoom.getBuyer().getId(),
                    chatRoom.getLender().getId(),
                    chatRoom.getRental().getId()).orElse(null);
            RentalState rentalState = null;
            if (rentalInfo != null) {
                rentalState = rentalInfo.getState();
            }

            // 채팅방의 최근 메시지 불러오기
            Optional<ChatMessage> optionalMessage = chatMessageRepository.findFirstByRoomIdOrderByCreatedAtDesc(chatRoom.getId());
            String recentMessage = optionalMessage.map(ChatMessage::getMessage).orElse(null);

            // 대여글의 첫 번째 이미지 불러오기
            Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(chatRoom.getRental().getId());
            String imgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

            chatRoomResponseList.add(new RentalChatRoomListResponse(chatRoom, opponentSid, rentalState, recentMessage, imgUrl, opponent));
        }
        return chatRoomResponseList;
    }

    /* 대여글 채팅방 조회 (채팅 메시지 목록 포함) */
    public RentalChatRoomResponse getRentalChatRoom(Long roomId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findOneByIdAndRentalIdNotNull(roomId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_RENTAL_CHAT_ROOM, NOT_FOUND));

        // 채팅방 참여자인지 확인 (관리자는 가능)
        if (user.getIsAdmin() == Role.USER && checkChatRoomUser(chatRoom, user.getId())) {
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

        // 상대방의 id 암호화하기
        String opponentSid = aesUtil.encryptUserId(opponent.getId());

        // 첫 번째 이미지 불러오기
        Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(chatRoom.getRental().getId());
        String rentalImgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

        // 가격 정보 중에 제일 싼 가격 및 일수 불러오기
        int minPrice = 0;
        int minDays = 0;
        Optional<RentalPrice> minRentalPrice = rentalPriceRepository.findFirstByRentalOrderByPriceAscDaysAsc(chatRoom.getRental());
        if (minRentalPrice.isPresent()) {
            minPrice = minRentalPrice.get().getPrice();
            minDays = minRentalPrice.get().getDays();
        }

        // 옷 상태 체크 여부 불러오기
        boolean isChecked = rentalCheckRepository.existsByRoomId(roomId);

        // 최근 대여 정보에서 대여 상태 불러오기
        RentalState rentalState;
        RentalInfo rentalInfo = null;
        if (isChecked) {
            // 옷 상태 체크해야만 대여 정보 생성 가능
            rentalInfo = rentalInfoRepository.findFirstByBuyerIdAndLenderIdAndRentalIdOrderByRentalTimeDesc(
                    chatRoom.getBuyer().getId(),
                    chatRoom.getLender().getId(),
                    chatRoom.getRental().getId()).orElse(null);
            rentalState = rentalInfo == null ? null : rentalInfo.getState();
        }
        else {
            rentalState = null;
        }
        // 후기 작성 여부 불러오기
        boolean isReviewed;
        if (rentalInfo != null) {
            isReviewed = reviewRepository.existsByRentalInfoIdAndReviewerId(rentalInfo.getId(), user.getId());
        }
        else {
            isReviewed = false;
        }

        return new RentalChatRoomResponse(chatRoom, opponentSid, opponent, chatMessageResponseList,
                rentalImgUrl, minPrice, minDays, isChecked, rentalState, isReviewed);

    }

    /* 채팅 메시지 생성 후 DB에 저장 (대여글/유저 구분 X) */
    public ChatMessageResponse createChatMessage(ChatMessageRequest chatMessageRequest, ChatRoom chatRoom, User user) throws BaseException {

        user.checkIsFirstLogin();

        // 채팅방 참여자인지 확인
        if (checkChatRoomUser(chatRoom, user.getId())) {
            throw new BaseException(FORBIDDEN_ENTER_CHAT_ROOM, FORBIDDEN);
        }

        ChatMessage chatMessage = chatMessageRequest.toEntity(user, chatRoom);
        // 채팅방 최근 활성화된 시간 변경
        chatRoomRepository.save(chatRoom.updateRecentMessageTime());
        // 채팅 메시지 저장
        return new ChatMessageResponse(chatMessageRepository.save(chatMessage));
    }

    // 채팅방 참여자가 아니면 true
    public boolean checkChatRoomUser(ChatRoom chatRoom, Long userId) {
        return !chatRoom.getBuyer().getId().equals(userId) && !chatRoom.getLender().getId().equals(userId);
    }

}
