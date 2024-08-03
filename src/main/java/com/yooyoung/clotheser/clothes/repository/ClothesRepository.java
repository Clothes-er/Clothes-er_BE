package com.yooyoung.clotheser.clothes.repository;

import com.yooyoung.clotheser.clothes.domain.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesRepository extends JpaRepository<Clothes, Long> {

}
