package com.yooyoung.clotheser.clothes.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yooyoung.clotheser.clothes.domain.Clothes;
import com.yooyoung.clotheser.clothes.domain.QClothes;
import com.yooyoung.clotheser.global.entity.AgeFilter;
import com.yooyoung.clotheser.user.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClothesFilterService {

    private final JPAQueryFactory queryFactory;

    /* 보유 옷 목록 필터링 */
    public List<Clothes> getFilteredClothesList(User user, String search, String sort, List<Gender> gender, Integer minHeight,
                                                Integer maxHeight, List<AgeFilter> age, List<String> category, List<String> style) {

        QClothes qClothes = QClothes.clothes;
        QUser qUser = QUser.user;

        // 기본적인 쿼리
        JPAQuery<Clothes> query = queryFactory.selectFrom(qClothes)
                .leftJoin(qClothes.user, qUser)
                .where(qClothes.deletedAt.isNull())
                .where(qClothes.isPublic.isTrue())
                .where(qUser.isRestricted.isFalse())    // 이용 제한 회원 제외
                .where(qClothes.user.ne(user));

        // 검색
        if (search != null && !search.isEmpty()) {
            query.where(qClothes.name.containsIgnoreCase(search));
        }

        // 성별 필터링
        if (gender != null) {
            query.where(qClothes.gender.in(gender));
        }

        // 키 필터링 (한 쪽에만 조건 걸어도 가능)
        if (minHeight != null && maxHeight != null) {
            query.where(qUser.height.between(minHeight, maxHeight));
        } else if (minHeight != null) {
            query.where(qUser.height.goe(minHeight));
        } else if (maxHeight != null) {
            query.where(qUser.height.loe(maxHeight));
        }

        // 나이대 필터링
        BooleanExpression ageCondition = ageFilterCondition(qUser, age);
        if (ageCondition != null) {
            query.where(ageCondition);
        }

        // 카테고리 필터링
        if (category != null && !category.isEmpty()) {
            query.where(qClothes.category.in(category));
        }

        // 스타일 필터링
        if (style != null && !style.isEmpty()) {
            query.where(qClothes.style.in(style));
        }

        // 사용자 선호 카테고리 및 스타일 조회
        List<String> userFavCategories = getUserFavCategories(user.getId());
        List<String> userFavStyles = getUserFavStyles(user.getId());

        // 정렬
        List<Clothes> clothesList = query.fetch();

        // Comparator 생성
        Comparator<Clothes> comparator = null;

        if (sort != null && !sort.isEmpty()) {
            if (sort.equals("createdAt")) {
                // 최신순 정렬
                comparator = Comparator.comparing(Clothes::getCreatedAt).reversed();
            } else if (sort.equals("closetScore")) {
                // 옷장 점수 높은 순 정렬
                comparator = Comparator.comparing((Clothes c) -> c.getUser().getClosetScore()).reversed();
            }
        }

        if (comparator != null) {
            // 기본 정렬 후, 동일한 값일 경우 유사도 기준으로 정렬
            comparator = comparator.thenComparing(c -> calculateSimilarity(c, userFavCategories, userFavStyles), Comparator.reverseOrder());
        } else {
            // 정렬 옵션이 없을 경우 유사도 기준으로 정렬
            comparator = Comparator.comparingDouble((Clothes c) -> calculateSimilarity(c, userFavCategories, userFavStyles)).reversed();
        }

        // Comparator를 사용하여 정렬 수행
        return clothesList.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }


    /* 나이대 필터링 */
    private BooleanExpression ageFilterCondition(QUser qUser, List<AgeFilter> ageFilters) {
        if (ageFilters == null || ageFilters.isEmpty()) {
            return null;
        }

        BooleanExpression ageCondition = null;
        for (AgeFilter filter : ageFilters) {
            // 필터링
            BooleanExpression currentCondition = createAgeCondition(qUser.birthday, filter);
            ageCondition = (ageCondition == null) ? currentCondition : ageCondition.or(currentCondition);
        }

        return ageCondition;
    }

    private BooleanExpression createAgeCondition(DatePath<LocalDate> birthday, AgeFilter filter) {
        String baseCondition = "(YEAR(CURRENT_DATE) - YEAR({0}) - " +
                "(CASE WHEN MONTH(CURRENT_DATE) > MONTH({0}) " +
                "OR (MONTH(CURRENT_DATE) = MONTH({0}) " +
                "AND DAY(CURRENT_DATE) >= DAY({0})) " +
                "THEN 0 ELSE 1 END)) ";

        return switch (filter) {
            case TEENAGER -> Expressions.booleanTemplate(baseCondition + "<= 19", birthday);
            case EARLY_TWENTIES -> Expressions.booleanTemplate(baseCondition + "BETWEEN 20 AND 22", birthday);
            case MID_TWENTIES -> Expressions.booleanTemplate(baseCondition + "BETWEEN 23 AND 26", birthday);
            case LATE_TWENTIES -> Expressions.booleanTemplate(baseCondition + "BETWEEN 27 AND 29", birthday);
            case OTHER -> Expressions.booleanTemplate(baseCondition + ">= 30", birthday);
        };
    }

    /* 유사도 계산 */
    private double calculateSimilarity(Clothes clothes, List<String> userFavCategories, List<String> userFavStyles) {
        int similarityScore = 0;

        // 성별 유사도 계산
        if (clothes.getGender() == clothes.getGender()) {
            similarityScore += 1;
        }

        // 카테고리 유사도 계산
        if (userFavCategories.contains(clothes.getCategory())) {
            similarityScore += 1;
        }

        // 스타일 유사도 계산
        if (userFavStyles.contains(clothes.getStyle())) {
            similarityScore += 1;
        }

        return similarityScore;
    }

    /* 사용자 선호 카테고리 조회 */
    private List<String> getUserFavCategories(Long userId) {
        QFavClothes qFavClothes = QFavClothes.favClothes;
        return queryFactory.select(qFavClothes.category)
                .from(qFavClothes)
                .where(qFavClothes.user.id.eq(userId))
                .fetch();
    }

    /* 사용자 선호 스타일 조회 */
    private List<String> getUserFavStyles(Long userId) {
        QFavStyle qFavStyle = QFavStyle.favStyle;
        return queryFactory.select(qFavStyle.style)
                .from(qFavStyle)
                .where(qFavStyle.user.id.eq(userId))
                .fetch();
    }

}
