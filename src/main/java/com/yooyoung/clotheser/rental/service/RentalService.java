package com.yooyoung.clotheser.rental.service;

import com.yooyoung.clotheser.chat.domain.ChatRoom;
import com.yooyoung.clotheser.chat.repository.ChatRoomRepository;
import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.rental.domain.*;
import com.yooyoung.clotheser.rental.dto.*;
import com.yooyoung.clotheser.rental.repository.RentalImgRepository;
import com.yooyoung.clotheser.rental.repository.RentalInfoRepository;
import com.yooyoung.clotheser.rental.repository.RentalPriceRepository;
import com.yooyoung.clotheser.rental.repository.RentalRepository;
import com.yooyoung.clotheser.user.domain.User;
import com.yooyoung.clotheser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalService {

    private final S3Service s3Service;

    private final RentalRepository rentalRepository;
    private final RentalPriceRepository rentalPriceRepository;
    private final RentalImgRepository rentalImgRepository;
    private final RentalInfoRepository rentalInfoRepository;

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    /* 대여글 생성 */
    public RentalResponse createRentalPost(RentalRequest rentalRequest, MultipartFile[] images, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // TODO: 보유 옷으로부터 대여글 생성하는지 확인

        // TODO: 보유 옷 회원과 대여글 작성하려는 회원 일치 확인

        Rental rental = rentalRequest.toEntity(user);
        rentalRepository.save(rental);

        // 대여글 이미지들 저장
        List<String> imgUrls = s3Service.uploadImages(images, rental);

        // 대여글 가격표 생성
        for (int i = 0; i < rentalRequest.getPrices().size(); i++) {
            RentalPrice rentalPrice = RentalPrice.builder()
                    .rental(rental)
                    .price(rentalRequest.getPrices().get(i).getPrice())
                    .days(rentalRequest.getPrices().get(i).getDays())
                    .build();
            rentalPriceRepository.save(rentalPrice);
        }

        return new RentalResponse(user, rental, imgUrls, rentalRequest.getPrices());

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

        return new RentalResponse(user, rental, imgUrls, prices);
    }

    /* 대여글 목록 조회 */
    public List<RentalListReponse> getRentalList(User user, String search) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        double latitude = user.getLatitude();
        double longitude = user.getLongitude();

        // 검색 여부 확인
        List<Rental> rentalList = new ArrayList<>();
        if (search != null && !search.isEmpty()) {
            rentalList = rentalRepository.searchRentalsWithinDistance(search, latitude, longitude);
        }
        else {
            // 전체 조회
            rentalList = rentalRepository.findRentalsWithinDistance(latitude, longitude);
        }

        List<RentalListReponse> responses = new ArrayList<>();
        for (Rental rental : rentalList) {
            // 첫 번째 이미지 불러오기
            Optional<RentalImg> optionalImg = rentalImgRepository.findFirstByRentalId(rental.getId());
            String imgUrl = optionalImg.map(RentalImg::getImgUrl).orElse(null);

            // 가격 정보 중에 제일 싼 가격 불러오기
            Optional<Integer> optionalPrice = rentalPriceRepository.findMinPrice(rental);
            int minPrice = optionalPrice.orElse(0);

            responses.add(new RentalListReponse(rental, imgUrl, minPrice));
        }

        return responses;
    }

    /* 대여하기 */
    public RentalInfoResponse createRentalInfo(RentalInfoRequest rentalInfoRequest, Long roomId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // TODO: 옷 상태 체크했는지 확인, 대여 정보 테이블에 메모 추가

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

        // 판매자인지 확인
        if (!chatRoom.getLender().getId().equals(user.getId()) ) {
            throw new BaseException(FORBIDDEN_UPDATE_RENTAL_INFO, FORBIDDEN);
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
}
