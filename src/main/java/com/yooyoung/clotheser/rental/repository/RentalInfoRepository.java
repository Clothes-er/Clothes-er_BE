package com.yooyoung.clotheser.rental.repository;

import com.yooyoung.clotheser.rental.domain.RentalInfo;
import com.yooyoung.clotheser.rental.domain.RentalState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RentalInfoRepository extends JpaRepository<RentalInfo, Long> {

    // 최근 대여 정보 찾기
    Optional<RentalInfo> findFirstByBuyerIdAndLenderIdAndRentalIdOrderByRentalTimeDesc(Long buyerId, Long lenderId, Long rentalId);

    // 반납: 대여 중인 대여 정보 찾기
    Optional<RentalInfo> findFirstByBuyerIdAndLenderIdAndRentalIdAndState(Long buyerId, Long lenderId, Long rentalId, RentalState state);

    // 공유 내역 조회
    @Query("SELECT r FROM RentalInfo r WHERE r.lender.id = :userId ORDER BY " +
            "CASE WHEN r.state = com.yooyoung.clotheser.rental.domain.RentalState.RENTED THEN 0 ELSE 1 END, r.startDate DESC")
    List<RentalInfo> findAllByLenderIdOrderByStateAndRentalDate(@Param("userId") Long userId);

    // 대여 내역 조회
    @Query("SELECT r FROM RentalInfo r WHERE r.buyer.id = :userId ORDER BY " +
            "CASE WHEN r.state = com.yooyoung.clotheser.rental.domain.RentalState.RENTED THEN 0 ELSE 1 END, r.startDate DESC")
    List<RentalInfo> findAllByBuyerIdOrderByStateAndRentalDate(@Param("userId") Long userId);

    // 대여 중인지 확인
    boolean existsByRentalIdAndState(Long rentalId, RentalState state);

}
