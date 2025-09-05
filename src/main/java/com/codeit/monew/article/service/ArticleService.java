package com.codeit.monew.article.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.exception.article.ArticleException;
import com.codeit.monew.exception.article.ArticleNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ArticleService {

  private final ArticleRepository articleRepository;

  public ArticleService(ArticleRepository articleRepository) {
    this.articleRepository = articleRepository;
  }

  public void deleteArticle(UUID articleId) {
    log.info("기사 삭제 시작 {}", articleId);
    Article checkedArticle =  checkArticle(articleId);
    checkedArticle.setDeleted(true);
    articleRepository.save(checkedArticle);
    log.info("기사 삭제 완료 {}", articleId);
  }

  private Article checkArticle(UUID articleId) {
    log.info("기사 존재 검증 시작 {}", articleId);
    Optional<Article> articleOptional = articleRepository.findById(articleId);
    if (articleOptional.isEmpty()) {
      log.info("기사가 없습니다 {}", articleId);
      Map<String, Object> details = new HashMap<>();
      details.put("이유: ", articleId+"기사는 존재하지 않습니다." );
      throw new ArticleNotFoundException(details);
    }
    log.info("기사 존재 검증 완료 {}", articleId);
    return articleOptional.get();
  }
}