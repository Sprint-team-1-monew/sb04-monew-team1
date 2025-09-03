package com.codeit.monew.comment.entity;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.base.entity.BaseEntity;
import com.codeit.monew.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Comments")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor()
public class Comment extends BaseEntity {

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "deleted", nullable = false)
  private Boolean deleted;

  @Builder.Default
  @Column(name = "like_count", nullable = false)
  private int likeCount = 0;

  @Column(name = "deleted_at", updatable = false, nullable = false)
  private LocalDateTime deletedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id", nullable = false)
  private Article article;
}