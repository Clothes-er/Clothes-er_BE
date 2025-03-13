package com.yooyoung.clotheser.clothes.repository;

import com.yooyoung.clotheser.clothes.domain.ClothesLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClothesLikeRepository extends JpaRepository<ClothesLike, Long> {

    boolean existsByUserIdAndClothesIdAndDeletedAtNull(Long userId, Long clothesId);

    Optional<ClothesLike> findOneByUserIdAndClothesIdAndDeletedAtNull(Long userId, Long clothesId);

    int countByClothesIdAndDeletedAtNull(Long clothesId);

}
