package com.codeit.monew.article.repository;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticlesViewUser;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleViewUserRepository extends JpaRepository<ArticlesViewUser, UUID> {
  boolean existsByArticleAndUser(Article article, User user);
}