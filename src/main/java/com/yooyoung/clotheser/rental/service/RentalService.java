package com.yooyoung.clotheser.rental.service;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.domain.RentalImg;
import com.yooyoung.clotheser.rental.domain.RentalPrice;
import com.yooyoung.clotheser.rental.dto.PostRentalRequest;
import com.yooyoung.clotheser.rental.dto.RentalListReponse;
import com.yooyoung.clotheser.rental.dto.RentalPriceDto;
import com.yooyoung.clotheser.rental.dto.RentalResponse;
import com.yooyoung.clotheser.rental.repository.RentalImgRepository;
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
public class RentalService {

    private final RentalRepository rentalRepository;
    private final RentalPriceRepository rentalPriceRepository;
    private final RentalImgRepository rentalImgRepository;

    // 대여글 생성
    public RentalResponse createRentalPost(PostRentalRequest postRentalRequest, Long clothesId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // TODO: 보유 옷으로부터 대여글 생성하는지 확인

        // TODO: 보유 옷 회원과 대여글 작성하려는 회원 일치 확인

        Rental rental = postRentalRequest.toEntity(user, clothesId);
        rentalRepository.save(rental);

        // TODO: 대여글 이미지 생성

        // 대여글 가격표 생성
        for (int i = 0; i < postRentalRequest.getPrices().size(); i++) {
            RentalPrice rentalPrice = RentalPrice.builder()
                    .rental(rental)
                    .price(postRentalRequest.getPrices().get(i).getPrice())
                    .days(postRentalRequest.getPrices().get(i).getDays())
                    .build();
            rentalPriceRepository.save(rentalPrice);
        }

        return new RentalResponse(user, rental, postRentalRequest.getPrices());

    }

    // 대여글 조회
    public RentalResponse getRental(Long rentalId, User user) throws BaseException {

        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        // 대여글 존재 확인
        Rental rental = rentalRepository.findByIdAndDeletedAtNull(rentalId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_RENTAL, NOT_FOUND));

        // 가격 정보 불러오기
        List<RentalPriceDto> prices = new ArrayList<>();
        List<RentalPrice> rentalPrices = rentalPriceRepository.findAllByRentalId(rentalId);
        for (RentalPrice rentalPrice : rentalPrices) {
            prices.add(new RentalPriceDto(rentalPrice.getDays(), rentalPrice.getPrice()));
        }

        return new RentalResponse(user, rental, prices);
    }

    // 대여글 목록 조회
    public List<RentalListReponse> getRentalList(User user) throws BaseException {
        // 최초 로그인이 아닌지 확인
        if (user.getIsFirstLogin()) {
            throw new BaseException(REQUEST_FIRST_LOGIN, FORBIDDEN);
        }

        double latitude = user.getLatitude();
        double longitude = user.getLongitude();

        List<Rental> rentalList = rentalRepository.findRentalsWithinDistance(latitude, longitude);
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
}
