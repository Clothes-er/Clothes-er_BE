package com.yooyoung.clotheser.clothes.repository;

import com.yooyoung.clotheser.clothes.domain.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClothesRepository extends JpaRepository<Clothes, Long> {

    // 보유 옷 조회
    Optional<Clothes> findByIdAndDeletedAtNull(Long id);

    // 나의 보유 옷 목록 조회
    List<Clothes> findAllByUserIdAndDeletedAtNullOrderByCreatedAtDesc(Long userId);

    // 남의 보유 옷 목록 조회
    List<Clothes> findAllByUserIdAndDeletedAtNullAndIsPublicTrueOrderByCreatedAtDesc(Long userId);

    // 대여글 작성 - 대여글 없는 보유 옷 목록 조회
    List<Clothes> findAllByUserIdAndRentalIdNullAndDeletedAtNullOrderByCreatedAtDesc(Long userId);

    // 찜한 보유 옷 목록 조회
    @Query("select c from Clothes c " +
            "join ClothesLike cl on c.id = cl.clothes.id " +
            "where cl.user.id = :userId and c.deletedAt is null and cl.deletedAt is null " +
            "order by cl.createdAt desc")
    List<Clothes> findLikeClothes(Long userId);
}
