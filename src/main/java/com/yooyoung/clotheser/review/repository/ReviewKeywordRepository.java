package com.yooyoung.clotheser.review.repository;

import com.yooyoung.clotheser.review.domain.ReviewKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewKeywordRepository extends JpaRepository<ReviewKeyword, Long> {

    // 키워드 가져오기
    List<ReviewKeyword> findAllByReviewId(Long reviewId);

    int countAllByReviewId(Long reviewId);
}
