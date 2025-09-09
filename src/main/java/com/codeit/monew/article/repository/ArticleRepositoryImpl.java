package com.codeit.monew.article.repository;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.QArticle;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.cglib.core.Local;

public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public ArticleRepositoryImpl(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  @Override
  public List<Article> searchArticles(
      String keyword,
      UUID interestId,
      List<String> sourceIn,
      LocalDateTime publishDateFrom,
      LocalDateTime publishDateTo,
      String orderBy,
      String direction,
      String cursor,
      LocalDateTime after,
      int limit
  ) {
    QArticle q = QArticle.article;

    // 1) 기본 조건
    BooleanBuilder where = new BooleanBuilder();
    where.and(q.deleted.isFalse());

    if (isValid(keyword)) {
      where.and(q.articleTitle.containsIgnoreCase(keyword)
          .or(q.articleSummary.containsIgnoreCase(keyword)));
    }
    if (interestId != null) {
      where.and(q.interest.id.eq(interestId));
    }
    if (sourceIn != null && !sourceIn.isEmpty()) {
      where.and(q.source.in(sourceIn));
    }
    if (publishDateFrom != null && publishDateTo != null) {
      where.and(q.articlePublishDate.between(publishDateFrom, publishDateTo));
    }

    // 2) 정렬/커서
    Order dir = isDesc(direction) ? Order.DESC : Order.ASC;
    List<OrderSpecifier<?>> orders = buildOrderSpecifiers(orderBy, dir, q);

    if (cursor != null && !cursor.isBlank()) {
      where.and(buildCursorCondition(cursor, orderBy, dir, q, after));
    } else if (after != null) {
      where.and(q.createdAt.gt(after));
    }

    // 3) 조회 (limit + 1은 호출부에서 hasNext 판단 시 사용)
    List<Article> rows = queryFactory
        .selectFrom(q)
        .where(where)
        .orderBy(orders.toArray(OrderSpecifier[]::new))
        .limit(limit + 1)
        .fetch();

    return rows;
  }

  @Override
  public long searchArticlesCount (
      String keyword,
      UUID interestId,
      List<String> sourceIn,
      LocalDateTime publishDateFrom,
      LocalDateTime publishDateTo
  ){
    QArticle q = QArticle.article;

    // 1) 기본 조건
    BooleanBuilder where = new BooleanBuilder();
    where.and(q.deleted.isFalse());

    if (isValid(keyword)) {
      where.and(q.articleTitle.containsIgnoreCase(keyword)
          .or(q.articleSummary.containsIgnoreCase(keyword)));
    }
    if (interestId != null) {
      where.and(q.interest.id.eq(interestId));
    }
    if (sourceIn != null && !sourceIn.isEmpty()) {
      where.and(q.source.in(sourceIn));
    }
    if (publishDateFrom != null && publishDateTo != null) {
      where.and(q.articlePublishDate.between(publishDateFrom, publishDateTo));
    }

    Long totalElements = queryFactory
        .select(q.count())
        .from(q)
        .where(where)
        .fetchOne();

    if (totalElements == null) {
      totalElements = 0L;  // null일 경우 기본값 처리
    }
    return totalElements;
  }

  private boolean isValid(String s) {
    return s != null && !s.trim().isEmpty();
  }

  private boolean isDesc(String direction) {
    return "DESC".equalsIgnoreCase(direction);
  }

  private List<OrderSpecifier<?>> buildOrderSpecifiers(String orderBy, Order dir, QArticle q) {
    List<OrderSpecifier<?>> orders = new ArrayList<>();

    switch (orderBy == null ? "" : orderBy) {
      case "commentCount":
        orders.add(new OrderSpecifier<>(dir, q.articleCommentCount));
        break;
      case "viewCount":
        orders.add(new OrderSpecifier<>(dir, q.articleViewCount));
        break;
      case "publishDate":
        orders.add(new OrderSpecifier<>(dir, q.articlePublishDate));
        break;
      default:
        orders.add(new OrderSpecifier<>(Order.DESC, q.createdAt));
        break;
    }

    orders.add(new OrderSpecifier<>(dir, q.createdAt));
    return orders;
  }

  private BooleanExpression buildCursorCondition(String cursor, String orderBy, Order dir, QArticle qArticle, LocalDateTime after) {
    // 비교 헬퍼
    switch (orderBy == null ? "" : orderBy) {
      case "commentCount": {
        BooleanExpression primaryCmp =
            (dir == Order.DESC) ? qArticle.articleCommentCount.lt(Long.valueOf(cursor))
                : qArticle.articleCommentCount.gt(Long.valueOf(cursor));
        return primaryCmp
            .or(qArticle.createdAt.eq(after).or(qArticle.createdAt.after(after)));
      }
      case "viewCount": {
        BooleanExpression primaryCmp =
            (dir == Order.DESC) ? qArticle.articleViewCount.lt(Long.valueOf(cursor))
                : qArticle.articleViewCount.gt(Long.valueOf(cursor));
        return primaryCmp
            .or(qArticle.createdAt.eq(after).or(qArticle.createdAt.after(after)));
      }
      case "publishDate": {
        BooleanExpression primaryCmp =
            (dir == Order.DESC) ? qArticle.articlePublishDate.lt(LocalDateTime.parse(cursor))
                : qArticle.articlePublishDate.gt(LocalDateTime.parse(cursor));
        return primaryCmp
            .or(qArticle.createdAt.eq(after).or(qArticle.createdAt.after(after)));
      }
    }
    return null;
  }
}