package com.yooyoung.clotheser.rental.repository;

import com.yooyoung.clotheser.rental.domain.RentalLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RentalLikeRepository extends JpaRepository<RentalLike, Long> {

    boolean existsByUserIdAndRentalIdAndDeletedAtNull(Long userId, Long rentalId);

    Optional<RentalLike> findOneByUserIdAndRentalIdAndDeletedAtNull(Long userId, Long rentalId);

    int countByRentalIdAndDeletedAtNull(Long rentalId);
}
