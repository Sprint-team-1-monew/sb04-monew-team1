package com.codeit.monew.comment.repository;

import com.codeit.monew.comment.entity.QCommentLike;
import com.querydsl.jpa.impl.JPADeleteClause;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentLikeQuerydslRepository {
  private final EntityManager em;

  public long deleteByCommentId(UUID commentId) {
    QCommentLike qCommentLike = QCommentLike.commentLike;

    return new JPADeleteClause(em, qCommentLike)
        .where(qCommentLike.comment.id.eq(commentId))
        .execute();
  }
}
