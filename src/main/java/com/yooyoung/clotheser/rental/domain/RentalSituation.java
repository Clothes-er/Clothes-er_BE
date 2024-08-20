package com.yooyoung.clotheser.rental.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum RentalSituation {
    INTERVIEW(Arrays.asList("면접", "비즈니스", "오피스", "회사", "정장", "구두")),
    GRADUATION(Arrays.asList("학위복", "졸업", "졸업사진", "대학", "고등", "중등", "초등")),
    WEDDING(Arrays.asList("신부룩", "신랑룩", "웨딩", "웨딩스냅", "드레스")),
    SPRING(Arrays.asList("봄", "황사", "꽃샘추위", "나들이", "환절기", "셔츠", "가디건")),
    VACATION(Arrays.asList("물놀이", "수영", "바다", "방수", "오리발", "비키니", "래쉬가드", "여행", "휴가", "쪼리", "샌들", "방학", "스노쿨링")),
    MONSOON(Arrays.asList("장마", "소나기", "비바람", "장화", "우비", "반바지", "크록스")),
    AUTUMN(Arrays.asList("가을", "쌀쌀", "바람", "가죽자켓", "라이더자켓", "트렌치코트")),
    WINTER(Arrays.asList("겨울", "추위", "한파", "패딩", "니트", "장갑", "어그부츠", "목도리", "히트택")),
    EVENT(Arrays.asList("파티", "하객룩", "행사", "모임", "한복", "트위드", "블라우스", "원피스", "코스프레", "결혼식", "돌잔치"));

    private final List<String> keywords;

    RentalSituation(List<String> keywords) {
        this.keywords = keywords;
    }

}
