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
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "comments")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor()
public class Comment extends BaseEntity {

  @Column(name = "content", nullable = false)
  private String content;

  @Builder.Default
  @Column(name = "deleted", nullable = false)
  @Setter
  private Boolean isDeleted = false;

  @Builder.Default
  @Column(name = "like_count", nullable = false)
  private int likeCount = 0;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  @Setter
  private LocalDateTime deletedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id", nullable = false)
  private Article article;

  public void updateContent(String content) {
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("댓글 내용은 비어있을 수 없습니다.");
    }
    this.content = content;
    this.updatedAt = LocalDateTime.now();
  }
}