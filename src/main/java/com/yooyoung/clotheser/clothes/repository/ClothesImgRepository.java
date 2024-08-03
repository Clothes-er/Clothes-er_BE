package com.yooyoung.clotheser.clothes.repository;

import com.yooyoung.clotheser.clothes.domain.ClothesImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesImgRepository extends JpaRepository<ClothesImg, Long> {
}
