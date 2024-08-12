package com.yooyoung.clotheser.rental.service;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.chat.repository.ChatRoomRepository;
import com.yooyoung.clotheser.closet.dto.UserRentalListResponse;
import com.yooyoung.clotheser.clothes.domain.Clothes;
import com.yooyoung.clotheser.clothes.repository.ClothesRepository;
import com.yooyoung.clotheser.global.entity.AgeFilter;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.entity.BaseResponseStatus;
import com.yooyoung.clotheser.global.util.AESUtil;
import com.yooyoung.clotheser.global.util.Base64UrlSafeUtil;
import com.yooyoung.clotheser.rental.domain.*;
import com.yooyoung.clotheser.rental.dto.*;
import com.yooyoung.clotheser.rental.repository.*;
import com.yooyoung.clotheser.user.domain.Gender;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalService {

    @Autowired
    private AESUtil aesUtil;
    @Value("${aes.key}")
    private String AES_KEY;

    private final RentalImageService rentalImageService;
    private final RentalFilterService rentalFilterService;

    private final RentalRepository rentalRepository;
    private final RentalPriceRepository rentalPriceRepository;
    private final RentalImgRepository rentalImgRepository;
    private final RentalInfoRepository rentalInfoRepository;

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final RentalCheckRepository rentalCheckRepository;
    private final ClothesRepository clothesRepository;

    /* 보유 옷이 없는 나의 대여글 목록 조회 */
    public List<UserRentalListResponse> getMyNoClothesRentals(User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 대여글 목록 불러오기
        List<Rental> myRentals = rentalRepository.findAllByUserIdAndClothesIdNullAndDeletedAtNullOrderByCreatedAtDesc(user.getId());

        List<UserRentalListResponse> responses = new ArrayList<>();
        for (Rental rental : myRentals) {
            // 첫 번째 이미지 불러오기
            Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(rental.getId());
            String imgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

            // 가격 정보 중에 제일 싼 가격 불러오기
            Optional<Integer> optionalPrice = rentalPriceRepository.findMinPrice(rental);
            int minPrice = optionalPrice.orElse(0);

            responses.add(new UserRentalListResponse(rental, imgUrl, minPrice));
        }

        return responses;

    }

    /* 대여글 생성 */
    public RentalResponse createRental(RentalRequest rentalRequest, MultipartFile[] images, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 보유 옷에서 대여글을 작성하려는 경우
        Long clothesId = rentalRequest.getClothesId();
        boolean hasClothes = false;
        Clothes clothes = null;
        if (clothesId != null && clothesId > 0) {
            // 보유 옷 존재 확인
            clothes = clothesRepository.findByIdAndDeletedAtNull(clothesId)
                    .orElseThrow(() -> new BaseException(NOT_FOUND_CLOTHES, NOT_FOUND));

            // 보유 옷 회원과 대여글 작성하려는 회원 일치 확인
            if (!clothes.getUser().getId().equals(user.getId())) {
                throw new BaseException(FORBIDDEN_CREATE_RENTAL, FORBIDDEN);
            }

            // 이미 연동된 보유 옷인지 확인
            if (clothes.getRentalId() != null) {
                throw new BaseException(CLOTHES_HAS_RENTAL, CONFLICT);
            }

            hasClothes = true;
        }

        // 대여글 저장
        Rental rental = rentalRequest.toEntity(user);
        rentalRepository.save(rental);

        // 보유 옷 있는 경우 대여글 id 저장
        if (hasClothes) {
            Clothes updatedClothes = clothes.updateRental(rental.getId());
            clothesRepository.save(updatedClothes);
        }

        // 대여글 이미지들 저장
        List<String> imgUrls = rentalImageService.uploadImages(images, rental);

        // 대여글 가격표 생성
        for (int i = 0; i < rentalRequest.getPrices().size(); i++) {
            RentalPrice rentalPrice = RentalPrice.builder()
                    .rental(rental)
                    .price(rentalRequest.getPrices().get(i).getPrice())
                    .days(rentalRequest.getPrices().get(i).getDays())
                    .build();
            rentalPriceRepository.save(rentalPrice);
        }

        // 본인의 id 암호화하기
        String userSid;
        try {
            String encodedUserId = aesUtil.encrypt(String.valueOf(user.getId()), AES_KEY);
            userSid = Base64UrlSafeUtil.encode(encodedUserId);
        } catch (Exception e) {
            throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
        }

        return new RentalResponse(user, userSid, rental, imgUrls, rentalRequest.getPrices());

    }

    /* 대여글 조회 */
    public RentalResponse getRental(Long rentalId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 대여글 존재 확인
        Rental rental = rentalRepository.findByIdAndDeletedAtNull(rentalId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_RENTAL, NOT_FOUND));

        // 대여글 이미지들 url 불러오기
        List<String> imgUrls = new ArrayList<>();
        List<RentalImg> rentalImgs = rentalImgRepository.findByRentalId(rentalId);

        for (RentalImg rentalImg : rentalImgs) {
            imgUrls.add(rentalImg.getImgUrl());
        }

        // 기간이 적은 순으로 가격 정보 불러오기
        List<RentalPriceDto> prices = new ArrayList<>();
        List<RentalPrice> rentalPrices = rentalPriceRepository.findAllByRentalIdOrderByDays(rentalId);
        for (RentalPrice rentalPrice : rentalPrices) {
            prices.add(new RentalPriceDto(rentalPrice.getDays(), rentalPrice.getPrice()));
        }

        // 대여글 작성자의 id 암호화하기
        String userSid;
        try {
            String encodedUserId = aesUtil.encrypt(String.valueOf(rental.getUser().getId()), AES_KEY);
            userSid = Base64UrlSafeUtil.encode(encodedUserId);
        } catch (Exception e) {
            throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
        }

        return new RentalResponse(user, userSid, rental, imgUrls, prices);
    }

    /* 대여글 목록 조회 */
    public List<RentalListResponse> getRentalList(User user, String search, String sort, List<Gender> gender, Integer minHeight,
                                                  Integer maxHeight, List<AgeFilter> age, List<String> category, List<String> style) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 필터링된 대여글 목록 불러오기
        List<Rental> rentalList = rentalFilterService.getFilteredRentals(user, search, sort, gender,
                minHeight, maxHeight, age, category, style);

        List<RentalListResponse> responses = new ArrayList<>();
        for (Rental rental : rentalList) {
            // userId 암호화하기
            String userSid;
            try {
                String encodedUserId = aesUtil.encrypt(String.valueOf(rental.getUser().getId()), AES_KEY);
                userSid = Base64UrlSafeUtil.encode(encodedUserId);
            } catch (Exception e) {
                throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
            }

            // 첫 번째 이미지 불러오기
            Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(rental.getId());
            String imgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

            // 가격 정보 중에 제일 싼 가격 불러오기
            Optional<Integer> optionalPrice = rentalPriceRepository.findMinPrice(rental);
            int minPrice = optionalPrice.orElse(0);

            responses.add(new RentalListResponse(rental, userSid, imgUrl, minPrice));
        }

        return responses;
    }

    /* 옷 상태 체크하기 */
    public RentalCheckResponse createRentalCheck(RentalCheckRequest rentalCheckRequest, Long roomId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_CHAT_ROOM, NOT_FOUND));

        // 기존에 체크 내역이 있는지 확인
        if (rentalCheckRepository.existsByRoomId(roomId)) {
            throw new BaseException(RENTAL_CHECK_EXISTS, CONFLICT);
        }

        // 대여자인지 확인
        if (!chatRoom.getBuyer().getId().equals(user.getId()) ) {
            throw new BaseException(FORBIDDEN_CREATE_RENTAL_CHECK, FORBIDDEN);
        }

        // 옷 상태 기록 저장
        List<String> checkList = rentalCheckRequest.getCheckList();
        for (String check : checkList) {
            RentalCheck rentalCheck = RentalCheck.builder()
                    .clothesCheck(check)
                    .room(chatRoom)
                    .build();
            rentalCheckRepository.save(rentalCheck);
        }

        return new RentalCheckResponse(roomId, checkList);

    }

    /* 옷 상태 체크 내역 조회 */
    public RentalCheckResponse getRentalCheck(Long roomId, User user) throws BaseException {

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

        // 체크리스트 가져오기
        List<RentalCheck> rentalChecks = rentalCheckRepository.findAllByRoomId(roomId);
        List<String> checkList = rentalChecks.stream().map(RentalCheck::getClothesCheck).collect(Collectors.toCollection(ArrayList::new));

        return new RentalCheckResponse(roomId, checkList);
    }

    /* 대여하기 */
    public RentalInfoResponse createRentalInfo(RentalInfoRequest rentalInfoRequest, Long roomId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 옷 상태 체크했는지 확인
        if (!rentalCheckRepository.existsByRoomId(roomId)) {
            throw new BaseException(REQUEST_RENTAL_CHECK, UNAUTHORIZED);
        }

        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_CHAT_ROOM, NOT_FOUND));

        // 판매자인지 확인
        if (!chatRoom.getLender().getId().equals(user.getId()) ) {
            throw new BaseException(FORBIDDEN_CREATE_RENTAL_INFO, FORBIDDEN);
        }

        // 대여 정보 생성
        RentalInfo rentalInfo = rentalInfoRequest.toEntity(chatRoom);
        rentalInfoRepository.save(rentalInfo);

        return new RentalInfoResponse(rentalInfo);
    }

    /* 반납하기 */
    public RentalInfoResponse updateRentalInfo(Long roomId, User user) throws BaseException {

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

        // 대여 상태인 대여 정보 불러오기
        RentalInfo rentalInfo = rentalInfoRepository.findFirstByBuyerIdAndLenderIdAndRentalIdAndState(
                chatRoom.getBuyer().getId(),
                chatRoom.getLender().getId(),
                chatRoom.getRental().getId(),
                RentalState.RENTED
        ).orElseThrow(() -> new BaseException(NOT_FOUND_RENTAL_INFO, NOT_FOUND));

        // 대여 상태 반납으로 변경
        rentalInfoRepository.save(rentalInfo.updateRentalState());

        // 대여자 대여 횟수 증가
        userRepository.save(rentalInfo.getBuyer().increaseRentalCount());

        return new RentalInfoResponse(rentalInfo);
    }

    /* 대여글 수정 */
    public RentalResponse updateRental(RentalRequest rentalRequest, MultipartFile[] images, User user, Long rentalId) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 대여글 불러오기
        Rental rental = rentalRepository.findByIdAndDeletedAtNull(rentalId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_RENTAL, NOT_FOUND));

        // 본인의 대여글인지 확인
        if (!user.getId().equals(rental.getUser().getId())) {
            throw new BaseException(FORBIDDEN_USER, FORBIDDEN);
        }

        // 보유 옷 연결을 변경하는 경우
        Long originalClothesId = rental.getClothesId();
        Clothes originalClothes = clothesRepository.findByIdAndDeletedAtNull(originalClothesId).orElse(null);

        Long newClothesId = rentalRequest.getClothesId();
        Clothes newClothes = null;
        // 새로운 보유 옷 오류 검사
        if (newClothesId != null && newClothesId > 0) {
            // 보유 옷 존재 확인
            newClothes = clothesRepository.findByIdAndDeletedAtNull(newClothesId)
                    .orElseThrow(() -> new BaseException(NOT_FOUND_CLOTHES, NOT_FOUND));

            // 보유 옷 회원과 대여글 작성하려는 회원 일치 확인
            if (!newClothes.getUser().getId().equals(user.getId())) {
                throw new BaseException(FORBIDDEN_UPDATE_RENTAL, FORBIDDEN);
            }
        }

        // 기존 보유 옷이 있었으면 대여글 연결 끊기
        if (originalClothes != null) {
            originalClothes = originalClothes.updateRental(null);
            clothesRepository.save(originalClothes);
        }
        // 새로운 보유 옷이 있으면 대여글 연결
        if (newClothes != null) {
            newClothes = newClothes.updateRental(rental.getId());
            clothesRepository.save(newClothes);
        }

        // 대여글 수정
        Rental updatedRental = rental.updateRental(rentalRequest, user);
        rentalRepository.save(updatedRental);

        // 대여글 이미지들 삭제 후 저장
        List<RentalImg> rentalImgs = rentalImgRepository.findByRentalId(rentalId);
        rentalImageService.deleteImages(rentalImgs);
        List<String> imgUrls = rentalImageService.uploadImages(images, updatedRental);

        // 대여글 가격 삭제
        List<Long> rentalPriceIds = rentalPriceRepository.findAllByRentalIdOrderByDays(rentalId).stream()
                .map(RentalPrice::getId)
                .toList();
        rentalPriceRepository.deleteAllByIdInBatch(rentalPriceIds);

        // 새로운 가격표 생성
        for (int i = 0; i < rentalRequest.getPrices().size(); i++) {
            RentalPrice rentalPrice = RentalPrice.builder()
                    .rental(updatedRental)
                    .price(rentalRequest.getPrices().get(i).getPrice())
                    .days(rentalRequest.getPrices().get(i).getDays())
                    .build();
            rentalPriceRepository.save(rentalPrice);
        }

        // 본인의 id 암호화하기
        String userSid;
        try {
            String encodedUserId = aesUtil.encrypt(String.valueOf(user.getId()), AES_KEY);
            userSid = Base64UrlSafeUtil.encode(encodedUserId);
        } catch (Exception e) {
            throw new BaseException(FAIL_TO_ENCRYPT, INTERNAL_SERVER_ERROR);
        }

        return new RentalResponse(user, userSid, updatedRental, imgUrls, rentalRequest.getPrices());
    }

    /* 대여글 삭제 */
    public BaseResponseStatus deleteRental(Long rentalId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 대여글 불러오기
        Rental rental = rentalRepository.findByIdAndDeletedAtNull(rentalId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_RENTAL, NOT_FOUND));

        // 본인의 대여글인지 확인
        if (!user.getId().equals(rental.getUser().getId())) {
            throw new BaseException(FORBIDDEN_USER, FORBIDDEN);
        }

        // 대여중인지 확인
        if (rentalInfoRepository.existsByRentalIdAndState(rentalId, RentalState.RENTED)) {
            throw new BaseException(FORBIDDEN_DELETE_RENTAL, FORBIDDEN);
        }

        // 대여글 논리 삭제
        Rental deletedRental = rental.deleteRental();
        rentalRepository.save(deletedRental);

        // 대여글 이미지들 물리 삭제
        List<RentalImg> rentalImgs = rentalImgRepository.findByRentalId(rentalId);
        rentalImageService.deleteImages(rentalImgs);

        // 대여글 가격표 물리 삭제
        List<Long> rentalPriceIds = rentalPriceRepository.findAllByRentalIdOrderByDays(rentalId).stream()
                .map(RentalPrice::getId)
                .toList();
        rentalPriceRepository.deleteAllByIdInBatch(rentalPriceIds);

        return SUCCESS;
    }

}
