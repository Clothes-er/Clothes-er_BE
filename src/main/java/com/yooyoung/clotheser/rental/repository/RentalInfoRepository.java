package com.yooyoung.clotheser.rental.repository;

import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.rental.domain.RentalState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RentalInfoRepository extends JpaRepository<RentalInfo, Long> {

    // 채팅방 조회: 제일 최근 대여 정보에서 대여 상태 불러오기
    Optional<RentalInfo> findFirstByBuyerIdAndLenderIdAndRentalIdOrderByRentalTimeDesc(Long buyerId, Long lenderId, Long rentalId);

    // 반납: 대여 중인 대여 정보 찾기
    Optional<RentalInfo> findFirstByBuyerIdAndLenderIdAndRentalIdAndState(Long buyerId, Long lenderId, Long rentalId, RentalState state);

}
