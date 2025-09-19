package com.codeit.monew.article.entity;

import com.codeit.monew.base.entity.BaseEntity;
import com.codeit.monew.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "articles_view_user",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_articles_view_user_article_user",
        columnNames = {"article_id", "user_id"}
    )
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor()
public class ArticlesViewUser extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "article_id")
  private Article article;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;
}
