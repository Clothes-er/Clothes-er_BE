package com.yooyoung.clotheser.user.repository;

import com.yooyoung.clotheser.user.domain.BodyShape;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BodyShapeRepository extends JpaRepository<BodyShape, Long> {
}
