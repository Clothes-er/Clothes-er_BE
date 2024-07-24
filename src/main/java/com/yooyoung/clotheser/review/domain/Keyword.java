package com.yooyoung.clotheser.review.domain;

import lombok.Getter;

public enum Keyword {

    /*
        긍정 키워드
    */
    // 1. 공통
    KIND(true, "common", 1, "친절해요"),
    ON_TIME(true, "common", 2, "시간 약속을 잘 지켜요"),
    FAST_RESPONSE(true, "common", 1, "응답(연락)이 빨라요"),

    // 2. 판매자
    GOOD_CLOTHES_STATE(true, "lender", 2, "옷 상태가 좋아요"),
    CHEAP_PRICE(true, "lender", 1, "대여 가격이 비교적 저렴해요"),
    DETAIL_DESCRIPTION(true, "lender", 1, "상품 설명이 자세해요"),
    MATCHED_DESCRIPTION(true, "lender", 2, "상품 설명과 실제 상품이 동일해요"),

    // 3. 대여자
    CLEAN_RETURN(true, "buyer", 2, "깨끗하게 입고 반납했어요"),
    ON_RETURN_DATE(true, "buyer", 2, "반납일을 잘 지켜요"),

    /*
        부정 키워드
    */
    // 1. 공통
    UNKIND(false, "common", -1, "불친절해요"),
    NOT_ON_TIME(false, "common", -3, "시간 약속을 어겼어요"),
    SLOW_RESPONSE(false, "common", -2, "연락을 잘 받지 않아요"),

    // 2. 판매자
    BAD_CLOTHES_STATE(false, "lender", -3, "옷 상태가 좋지 않아요"),
    EXPENSIVE_PRICE(false, "lender", -1, "대여 가격이 비싸요"),
    MISMATCHED_DESCRIPTION(false, "lender", -2, "상품 설명과 실제 상품이 달라요"),

    // 3. 대여자
    DIRTY_RETURN(false, "buyer", -3, "반납할 때 옷 상태가 좋지 않아요"),
    NOT_ON_RETURN_DATE(false, "buyer", -3, "반납일을 어겼어요");

    private final boolean isPositive;
    private final String userRole;
    @Getter
    private final int score;
    private final String description;

    Keyword(boolean isPositive, String userRole , int score, String description) {
        this.isPositive = isPositive;
        this.userRole = userRole;
        this.score = score;
        this.description = description;
    }

    public boolean getIsPositive() {
        return isPositive;
    }

}
