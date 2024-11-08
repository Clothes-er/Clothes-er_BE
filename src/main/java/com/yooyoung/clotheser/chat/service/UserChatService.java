package com.yooyoung.clotheser.chat.service;

import com.yooyoung.clotheser.chat.domain.ChatMessage;
import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.chat.dto.ChatMessageResponse;
import com.yooyoung.clotheser.chat.dto.UserChatRoomListResponse;
import com.yooyoung.clotheser.chat.dto.UserChatRoomResponse;
import com.yooyoung.clotheser.chat.repository.ChatMessageRepository;
import com.yooyoung.clotheser.chat.repository.ChatRoomRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.ChatRoomException;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.global.util.Base64UrlSafeUtil;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.repository.UserRepository;
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
public class UserChatService {

    private final AESUtil aesUtil;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    private final UserRepository userRepository;

    /* 유저 채팅방 생성 */
    public UserChatRoomResponse createUserChatRoom(String userSid, User user) throws BaseException, ChatRoomException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 유예된 회원 확인
        if (user.getIsSuspended()) {
            throw new BaseException(USE_RESTRICTED, FORBIDDEN);
        }

        // 문의 대상 회원 불러오기
        Long userId = aesUtil.decryptUserSid(userSid);

        // 본인인 경우 채팅방 생성 X
        if (user.getId().equals(userId)) {
            throw new BaseException(FORBIDDEN_CREATE_CHAT_ROOM_MYSELF, FORBIDDEN);
        }

        User lender = userRepository.findByIdAndDeletedAtNull(userId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER, NOT_FOUND));

        // 채팅방이 이미 존재하는지 확인 (이미 해당 유저와의 채팅방이 있는 경우 roomId 리턴)
        Optional<ChatRoom> existedChatRoom = chatRoomRepository.findOneByBuyerIdAndLenderIdAndRentalIdNull(user.getId(), lender.getId());
        if (existedChatRoom.isPresent()) {
            throw new ChatRoomException(USER_CHAT_ROOM_EXISTS, CONFLICT, existedChatRoom.get().getId());
        }

        // 유저 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .buyer(user)
                .lender(lender)
                .rental(null)
                .build();
        chatRoomRepository.save(chatRoom);

        return new UserChatRoomResponse(chatRoom.getId(), userSid, lender.getNickname());

    }

    /* 유저 채팅방 목록 조회 */
    public List<UserChatRoomListResponse> getUserChatRoomList(User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 유저 채팅방 목록 불러오기
        List<ChatRoom> userChatRooms = chatRoomRepository.findUserChatRoomsByUserId(user.getId());
        List<UserChatRoomListResponse> userChatRoomListResponses = new ArrayList<>();
        for (ChatRoom chatRoom : userChatRooms) {
            // 로그인한 회원이 대여자(문의 요청자)인지 판매자(문의 대상자)인지 구분
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
                String encodedUserId = aesUtil.encrypt(String.valueOf(opponent.getId()));
                opponentSid = Base64UrlSafeUtil.encode(encodedUserId);
            } catch (Exception e) {
                throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
            }

            // 채팅방의 최근 메시지 불러오기
            Optional<ChatMessage> optionalMessage = chatMessageRepository.findFirstByRoomIdOrderByCreatedAtDesc(chatRoom.getId());
            String recentMessage = optionalMessage.map(ChatMessage::getMessage).orElse(null);

            userChatRoomListResponses.add(new UserChatRoomListResponse(chatRoom, opponentSid, recentMessage, opponent));

        }

        return userChatRoomListResponses;

    }

    /* 유저 채팅방 조회 (채팅 메시지 목록 포함) */
    public UserChatRoomResponse getUserChatRoom(Long roomId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 유저 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findOneByIdAndRentalIdNull(roomId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER_CHAT_ROOM, NOT_FOUND));

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

        // 로그인한 회원이 대여자(문의 요청자)인지 판매자(문의 대상자)인지 구분
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
            String encodedUserId = aesUtil.encrypt(String.valueOf(opponent.getId()));
            opponentSid = Base64UrlSafeUtil.encode(encodedUserId);
        } catch (Exception e) {
            throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
        }

        return new UserChatRoomResponse(chatRoom.getId(), opponentSid, opponent, chatMessageResponseList);
    }

}
