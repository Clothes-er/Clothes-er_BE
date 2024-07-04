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

import com.yooyoung.clotheser.global.entity.ChatRoomException;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.global.util.Base64UrlSafeUtil;
import com.yooyoung.clotheser.rental.domain.*;
import com.yooyoung.clotheser.rental.repository.*;
import com.yooyoung.clotheser.user.domain.User;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private AESUtil aesUtil;
    @Value("${aes.key}")
    private String AES_KEY;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    private final RentalRepository rentalRepository;
    private final RentalImgRepository rentalImgRepository;
    private final RentalPriceRepository rentalPriceRepository;
    private final RentalInfoRepository rentalInfoRepository;
    private final RentalCheckRepository rentalCheckRepository;

    /* 채팅방 생성 */
    public ChatRoomResponse createChatRoom(Long rentalId, User user) throws BaseException, ChatRoomException {

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

        // 채팅방이 이미 존재하는지 확인 (대여자가 동일한 대여글에서 채팅방 하나만 가능) -> roomId 리턴
        Optional<ChatRoom> existedChatRoom = chatRoomRepository.findOneByBuyerIdAndRentalId(user.getId(), rentalId);
        if (existedChatRoom.isPresent()) {
            throw new ChatRoomException(CHAT_ROOM_EXISTS, CONFLICT, existedChatRoom.get().getId());

        }

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .buyer(user)
                .lender(rental.getUser())
                .rental(rental)
                .build();
        chatRoomRepository.save(chatRoom);

        // 판매자의 id 암호화하기
        String lenderSid;
        try {
            String encodedUserId = aesUtil.encrypt(String.valueOf(chatRoom.getLender().getId()), AES_KEY);
            lenderSid = Base64UrlSafeUtil.encode(encodedUserId);
        } catch (Exception e) {
            throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
        }

        // 첫 번째 이미지 불러오기
        Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(rental.getId());
        String rentalImgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

        // 가격 정보 중에 제일 싼 가격 불러오기
        Integer minPrice = rentalPriceRepository.findMinPrice(rental).orElse(null);

        return new ChatRoomResponse(chatRoom, lenderSid, chatRoom.getLender().getNickname(), rental, rentalImgUrl, minPrice);

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

            // 상대방의 id 암호화하기
            String opponentSid;
            try {
                String encodedUserId = aesUtil.encrypt(String.valueOf(opponent.getId()), AES_KEY);
                opponentSid = Base64UrlSafeUtil.encode(encodedUserId);
            } catch (Exception e) {
                throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
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

            chatRoomResponseList.add(new ChatRoomListResponse(chatRoom, opponentSid, rentalInfo, recentMessage, imgUrl, opponent));
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

        // 상대방의 id 암호화하기
        String opponentSid;
        try {
            String encodedUserId = aesUtil.encrypt(String.valueOf(opponent.getId()), AES_KEY);
            opponentSid = Base64UrlSafeUtil.encode(encodedUserId);
        } catch (Exception e) {
            throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
        }

        // 유저 기반 채팅방인지 확인
        if (chatRoom.getRental() == null) {
            return new ChatRoomResponse(chatRoom, opponentSid, opponent.getNickname(), chatMessageResponseList,
                    null, null, null, null);
        }

        // 첫 번째 이미지 불러오기
        Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(chatRoom.getRental().getId());
        String rentalImgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

        // 가격 정보 중에 제일 싼 가격 불러오기
        Integer minPrice = rentalPriceRepository.findMinPrice(chatRoom.getRental()).orElse(null);

        // 옷 상태 체크 여부 불러오기
        boolean isChecked = rentalCheckRepository.existsByRoomId(roomId);

        // 최근 대여 정보에서 대여 상태 불러오기
        RentalState rentalState;
        if (isChecked) {
            // 옷 상태 체크해야만 대여 정보 생성 가능
            RentalInfo rentalInfo = rentalInfoRepository.findFirstByBuyerIdAndLenderIdAndRentalIdOrderByRentalTimeDesc(
                    chatRoom.getBuyer().getId(),
                    chatRoom.getLender().getId(),
                    chatRoom.getRental().getId()).orElse(null);
            rentalState = rentalInfo == null ? null : rentalInfo.getState();
        }
        else {
            rentalState = null;
        }

        return new ChatRoomResponse(chatRoom, opponentSid, opponent.getNickname(), chatMessageResponseList,
                rentalImgUrl, minPrice, isChecked, rentalState);

    }
}
