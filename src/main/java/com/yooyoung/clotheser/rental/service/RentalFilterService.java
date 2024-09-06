package com.yooyoung.clotheser.rental.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yooyoung.clotheser.global.entity.AgeFilter;
import com.yooyoung.clotheser.rental.domain.QRental;
import com.yooyoung.clotheser.rental.domain.Rental;
import com.yooyoung.clotheser.rental.domain.RentalSituation;
import com.yooyoung.clotheser.user.domain.Gender;
import com.yooyoung.clotheser.user.domain.QUser;
import com.yooyoung.clotheser.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalFilterService {

    private final JPAQueryFactory queryFactory;

    /* 대여글 목록 필터링 */
    public List<Rental> getFilteredRentals(User user, String search, String sort, List<Gender> gender, Integer minHeight, Integer maxHeight,
                                           List<AgeFilter> age, List<String> category, List<String> style, RentalSituation situation) {

        Double latitude = user.getLatitude();    // 관리자 계정은 주소 없을 수도 있으므로 Wrapper 타입
        Double longitude = user.getLongitude();

        QRental qRental = QRental.rental;
        QUser qUser = QUser.user;

        // 기본적인 쿼리
        JPAQuery<Rental> query = queryFactory.selectFrom(qRental)
                .leftJoin(qRental.user, qUser)
                .where(qRental.deletedAt.isNull())
                .where(qUser.isSuspended.isFalse())
                .where(qUser.isRestricted.isFalse())
                .where(qUser.deletedAt.isNull())
                .where(distanceWithin(qUser, longitude, latitude));

        // 검색
        if (search != null && !search.isEmpty()) {
            query.where(qRental.title.containsIgnoreCase(search));
        }

        // 성별 필터링
        if (gender != null) {
            query.where(qRental.gender.in(gender));
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
            query.where(qRental.category.in(category));
        }

        // 스타일 필터링
        if (style != null && !style.isEmpty()) {
            query.where(qRental.style.in(style));
        }

        // 상황별 카테고리화
        if (situation != null) {
            List<String> keywords = situation.getKeywords();
            BooleanExpression keywordCondition = qRental.title.containsIgnoreCase(keywords.get(0))
                    .or(qRental.description.containsIgnoreCase(keywords.get(0)));

            for (int i = 1; i < keywords.size(); i++) {
                keywordCondition = keywordCondition.or(qRental.title.containsIgnoreCase(keywords.get(i)))
                        .or(qRental.description.containsIgnoreCase(keywords.get(i)));
            }
            query.where(keywordCondition);
        }

        // 정렬
        if (sort != null && !sort.isEmpty()) {
            if (sort.equals("createdAt")) {
                query.orderBy(qRental.createdAt.desc());
            }
            else if (sort.equals("closetScore")) {
                query.orderBy(qUser.closetScore.desc(), qRental.createdAt.desc());
            }
        }
        else {
            // 기본 쿼리는 최신순
            query.orderBy(qRental.createdAt.desc());
        }

        return query.fetch();
    }

    /* 주소 기반 대여글 목록 불러오기 */
    private BooleanExpression distanceWithin(QUser writer, Double longitude, Double latitude) {
        return Expressions.booleanTemplate("ST_Distance_Sphere(point({0}, {1}), point({2}, {3})) <= {4}",
                writer.longitude, writer.latitude, longitude, latitude, 2000);
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

}
