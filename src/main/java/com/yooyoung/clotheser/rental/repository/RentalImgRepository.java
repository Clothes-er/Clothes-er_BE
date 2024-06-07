package com.yooyoung.clotheser.rental.repository;

import com.yooyoung.clotheser.rental.domain.RentalImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalImgRepository extends JpaRepository<RentalImg, Long> {

    // 대여글의 모든 이미지들 가져오기
    List<RentalImg> findByRentalId(Long rentalId);

    // 대여글 첫 번째 이미지 가져오기
    Optional<RentalImg> findFirstByRentalId(Long rentalId);

}
