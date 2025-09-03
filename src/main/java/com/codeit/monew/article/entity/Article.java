package com.codeit.monew.article.entity;

import com.codeit.monew.base.entity.BaseEntity;
import com.codeit.monew.interest.entity.Interest;
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
@Table(name = "Article")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor()
public class Article extends BaseEntity {

  @Column(name = "source", nullable = false)
  private String source;

  @Column(name = "sourceUrl", nullable = false)
  private String sourceUrl;

  @Column(name = "article_title", nullable = false)
  private String articleTitle;

  @Column(name = "article_publish_date", nullable = false)
  private LocalDateTime articlePublishDate;

  @Column(name = "article_summary", nullable = false)
  private String articleSummary;

  @Column(name = "article_comment_count", nullable = false)
  private long articleCommentCount;

  @Column(name = "article_view_count", nullable = false)
  private long articleViewCount;

  @Column(name = "createdAt", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "interest_id")
  private Interest interest;
}