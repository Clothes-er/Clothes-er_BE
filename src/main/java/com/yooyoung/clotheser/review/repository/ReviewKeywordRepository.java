package com.yooyoung.clotheser.review.repository;

import com.yooyoung.clotheser.review.domain.ReviewKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewKeywordRepository extends JpaRepository<ReviewKeyword, Long> {

    // 키워드 가져오기
    List<ReviewKeyword> findAllByReviewId(Long reviewId);

    // 긍정 키워드 개수
    @Query(value = "SELECT COUNT(*) FROM review_keyword WHERE review_id = :reviewId " +
            "AND keyword BETWEEN 0 AND 8", nativeQuery = true)
    int countPositiveKeywordsByReviewId(Long reviewId);

    // 부정 키워드 개수
    @Query(value = "SELECT COUNT(*) FROM review_keyword WHERE review_id = :reviewId " +
            "AND keyword > 8", nativeQuery = true)
    int countNegativeKeywordsByReviewId(Long reviewId);

}
