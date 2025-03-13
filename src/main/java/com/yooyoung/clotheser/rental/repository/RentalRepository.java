package com.yooyoung.clotheser.rental.repository;

import com.yooyoung.clotheser.rental.domain.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    Optional<Rental> findByIdAndDeletedAtNull(Long id);

    // 나의 옷장
    List<Rental> findAllByUserIdAndDeletedAtNullOrderByCreatedAtDesc(Long userId);

    @Query("select r from Rental r " +
            "join RentalLike rl on r.id = rl.rental.id " +
            "where rl.user.id = :userId and r.deletedAt is null and rl.deletedAt is null " +
            "order by rl.createdAt desc")
    List<Rental> findLikeRentals(Long userId);

    // 보유 옷 등록 - 보유 옷 없는 대여글 목록 조회
    List<Rental> findAllByUserIdAndClothesIdNullAndDeletedAtNullOrderByCreatedAtDesc(Long userId);

}
