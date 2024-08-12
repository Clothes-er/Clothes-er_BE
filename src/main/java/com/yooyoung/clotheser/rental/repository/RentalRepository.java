package com.yooyoung.clotheser.rental.repository;

import com.yooyoung.clotheser.rental.domain.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    Optional<Rental> findByIdAndDeletedAtNull(Long id);

    // 나의 옷장
    List<Rental> findAllByUserIdAndDeletedAtNullOrderByCreatedAtDesc(Long userId);

    // 보유 옷 등록 - 보유 옷 없는 대여글 목록 조회
    List<Rental> findAllByUserIdAndClothesIdNullAndDeletedAtNullOrderByCreatedAtDesc(Long userId);

}
