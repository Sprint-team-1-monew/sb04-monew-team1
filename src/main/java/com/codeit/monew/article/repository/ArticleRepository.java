package com.codeit.monew.article.repository;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.interest.entity.Interest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {

  Optional<Article> findTop1ByInterestOrderByArticlePublishDateDesc(Interest interest);
  Optional<Article> findBySourceUrl(String sourceUrl);
}
