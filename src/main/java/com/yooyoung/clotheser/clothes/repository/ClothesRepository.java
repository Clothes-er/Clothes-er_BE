package com.yooyoung.clotheser.clothes.repository;

import com.yooyoung.clotheser.clothes.domain.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClothesRepository extends JpaRepository<Clothes, Long> {

    // 보유 옷 조회
    Optional<Clothes> findByIdAndDeletedAtNull(Long id);

    // 나의 옷장 - 보유 옷 목록 조회
    List<Clothes> findAllByUserIdAndDeletedAtNullOrderByCreatedAtDesc(Long userId);

}
