package com.codeit.monew.article.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.response_dto.CursorPageResponseArticleDto;
import com.codeit.monew.exception.article.ArticleException;
import com.codeit.monew.exception.article.ArticleNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
public class ArticleService {

  private final ArticleRepository articleRepository;

  public ArticleService(ArticleRepository articleRepository) {
    this.articleRepository = articleRepository;
  }

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



    log.info("뉴스 기사 검색 완료");
    return null;
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