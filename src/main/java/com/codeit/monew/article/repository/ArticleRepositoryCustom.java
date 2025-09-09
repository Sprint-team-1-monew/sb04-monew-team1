package com.codeit.monew.article.repository;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.response_dto.CursorPageResponseArticleDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepositoryCustom {

  List<Article> searchArticles(
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
  );

  long searchArticlesCount(
      String keyword,
      UUID interestId,
      List<String> sourceIn,
      LocalDateTime publishDateFrom,
      LocalDateTime publishDateTo
  );
}