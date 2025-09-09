package com.codeit.monew.comment.repository;

import static com.codeit.monew.comment.entity.QComment.comment;

import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentOrderBy;
import com.codeit.monew.comment.entity.QComment;
import com.codeit.monew.comment.entity.SortDirection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Comment> findComments(UUID articleId,
      CommentOrderBy orderBy,
      SortDirection direction,
      String cursor,
      LocalDateTime after,
      int limit) {

    QComment qComment = comment;

    BooleanBuilder where = new BooleanBuilder();
    where.and(comment.article.id.eq(articleId));

    if(cursor != null & after != null) {
      if(direction == SortDirection.ASC) {
        where.and(comment.createdAt.gt(after));
      } else{
        where.and(comment.createdAt.lt(after));
      }
    }

    OrderSpecifier<LocalDateTime> orderSpecifier =
        direction == SortDirection.ASC ?
                  comment.createdAt.asc() : comment.createdAt.desc();

    return queryFactory
        .selectFrom(comment)
        .where(where)
        .orderBy(orderSpecifier)
        .limit(limit)
        .fetch();
  }
}
