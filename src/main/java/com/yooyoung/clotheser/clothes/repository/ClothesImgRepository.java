package com.yooyoung.clotheser.clothes.repository;

import com.yooyoung.clotheser.clothes.domain.ClothesImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClothesImgRepository extends JpaRepository<ClothesImg, Long> {

    // 보유 옷의 이미지들 불러오기
    List<ClothesImg> findAllByClothesId(Long clothesId);

    // 보유 옷 첫 번째 이미지 불러오기
    Optional<ClothesImg> findFirstByClothesId(Long clothesId);

}
