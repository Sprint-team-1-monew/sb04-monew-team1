package com.codeit.monew.interest.repository;

import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.QInterest;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InterestRepositoryImpl implements InterestRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Interest> findInterestsWithCursor(
      String keyword,
      String orderBy,
      String direction,
      String cursor,
      LocalDateTime after,
      int limit
  ) {
    QInterest interest = QInterest.interest;

    // 정렬 기준 설정
    OrderSpecifier<?> orderSpecifier = getOrderSpecifier(orderBy, direction, interest);

    // 조건 설정
    BooleanExpression condition = interest.isDeleted.isFalse(); // 삭제되지 않은 것만

    if (isValid(keyword)) {
      condition = condition.and(interest.name.containsIgnoreCase(keyword));
    }

    if (cursor != null) {
      condition = condition.and(getCursorCondition(orderBy, direction, cursor, interest));
    } else if (after != null) {
      condition = condition.and(interest.createdAt.gt(after));
    }

    return queryFactory
        .selectFrom(interest)
        .where(condition)
        .orderBy(orderSpecifier)
        .limit(limit + 1)
        .fetch();
  }

  private boolean isValid(String keyword) {
    return keyword != null && !keyword.trim().isEmpty();
  }

  private OrderSpecifier<?> getOrderSpecifier(String orderBy, String direction, QInterest interest) {
    boolean isDesc = "desc".equalsIgnoreCase(direction);

    if ("subscriberCount".equals(orderBy)) {
      return isDesc ? interest.subscriberCount.desc() : interest.subscriberCount.asc();
    }
    return isDesc ? interest.name.desc() : interest.name.asc();
  }

  private BooleanExpression getCursorCondition(String orderBy, String direction, String cursor, QInterest interest) {
    boolean isDesc = "desc".equalsIgnoreCase(direction);

    if ("subscriberCount".equals(orderBy)) {
      int count = Integer.parseInt(cursor);
      return isDesc ? interest.subscriberCount.lt(count) : interest.subscriberCount.gt(count);
    }
    return isDesc ? interest.name.lt(cursor) : interest.name.gt(cursor);
  }
}



