package com.codeit.monew.article.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.mapper.ArticleMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.repository.ArticleViewUserRepository;
import com.codeit.monew.article.response_dto.ArticleDto;
import com.codeit.monew.article.response_dto.CursorPageResponseArticleDto;
import com.codeit.monew.exception.article.ArticleException;
import com.codeit.monew.exception.article.ArticleNotFoundException;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ArticleService {

  private final ArticleRepository articleRepository;
  private final ArticleMapper articleMapper;
  private final ArticleViewUserRepository articleViewUserRepository;
  private final UserRepository userRepository;

  public void softDeleteArticle(UUID articleId) {
    log.info("기사 논리 삭제 시작 {}", articleId);
    Article checkedArticle =  checkArticle(articleId);
    checkedArticle.setDeleted(true);
    articleRepository.save(checkedArticle);
    log.info("기사 논리 삭제 완료 {}", articleId);
  }

  public void hardDeleteArticle(UUID articleId) {
    log.info("기사 물리 삭제 시작 {}", articleId);
    Article checkedArticle =  checkArticle(articleId);
    articleRepository.delete(checkedArticle);
    log.info("기사 물리 삭제 완료 {}", articleId);
  }

  public CursorPageResponseArticleDto articleSearch(
      String keyword,
      UUID interestId,
      List<String> sourceIn,
      LocalDateTime publishDateFrom,
      LocalDateTime publishDateTo,
      String orderBy,
      String direction,
      String cursor,
      LocalDateTime after,
      int limit,
      UUID requestUserId
  ) {
    log.info("뉴스 기사 검색 시작 - keyword={}, interestId={}, sourceIn={}, publishDateFrom={}, publishDateTo={}, orderBy={}, direction={}, cursor={}, after={}, limit={}, requestUserId={}",
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo, orderBy, direction, cursor, after, limit, requestUserId);

    List<Article> articles = articleRepository.searchArticles(keyword, interestId, sourceIn, publishDateFrom, publishDateTo, orderBy, direction, cursor, after, limit);
    List<ArticleDto> content = new ArrayList<>();
    Optional<User> user = userRepository.findById(requestUserId); // npe 날 수 있으니 예외 처리, 충돌을 우려하며 나중에 유저 쪽 코드랑 머지 되었을 때 예외 처리 추가

    if (articles.size() > limit) {
      articles = articles.subList(0, limit);
    }

    for (Article article : articles) {
      boolean viewed = articleViewUserRepository.existsByArticleAndUser(article, user.get());
      ArticleDto dto = articleMapper.toDto(article, viewed);
      content.add(dto);
    }

    String nextCursor = null;
    LocalDateTime nextAfter = null;
    switch (orderBy == null ? "" : orderBy) {
      case "commentCount":
        nextCursor = articles.size() == limit+1 ? String.valueOf(articles.get(limit).getArticleCommentCount()) : null;
        nextAfter = articles.size() == limit+1 ? articles.get(limit).getCreatedAt() : null;
        break;
      case "viewCount":
        nextCursor = articles.size() == limit+1 ? String.valueOf(articles.get(limit).getArticleViewCount()) : null;
        nextAfter = articles.size() == limit+1 ? articles.get(limit).getCreatedAt() : null;
        break;
      case "publishDate":
        nextCursor = articles.size() == limit+1 ? String.valueOf(articles.get(limit).getArticlePublishDate()) : null;
        nextAfter = articles.size() == limit+1 ? articles.get(limit).getCreatedAt() : null;
        break;
    }
    int size = articles.size();
    long totalElements = articleRepository.searchArticlesCount(keyword, interestId, sourceIn, publishDateFrom, publishDateTo);
    boolean hasNex = size == limit+1;

    log.info("뉴스 기사 검색 완료");
    return new CursorPageResponseArticleDto(content, nextCursor, nextAfter, size, totalElements, hasNex);
  }

  private Article checkArticle(UUID articleId) {
    log.info("기사 존재 검증 시작 {}", articleId);
    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> {
          log.info("기사가 없습니다 {}", articleId);
          Map<String, Object> details = new HashMap<>();
          details.put("이유", articleId + " 기사는 존재하지 않습니다.");
          return new ArticleNotFoundException(details);
        });
    log.info("기사 존재 검증 완료 {}", articleId);
    return article;
  }
}
