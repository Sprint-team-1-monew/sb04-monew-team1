package com.codeit.monew.comment.repository.likeRepository;

import com.codeit.monew.comment.entity.QCommentLike;
import com.querydsl.jpa.impl.JPADeleteClause;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentLikeQuerydslRepositoryImpl implements CommentLikeQuerydslRepositoryCustom {

  private final EntityManager em;

  public long deleteByCommentId(UUID commentId) {
    QCommentLike qCommentLike = QCommentLike.commentLike;

    return new JPADeleteClause(em, qCommentLike)
        .where(qCommentLike.comment.id.eq(commentId))
        .execute();
  }
}
