package com.codeit.monew.article.controller;


import com.codeit.monew.article.service.ArticleService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

  private final ArticleService articleService;

  @DeleteMapping("/{articleId}")
  public ResponseEntity<?> softDeleteArticle(@PathVariable("articleId") String articleId) {
    log.info("기사 논리 삭제 요청 시작 {}", articleId);
    articleService.softDeleteArticle(UUID.fromString(articleId));
    log.info("기사 논리 삭제 요청 완료 {}", articleId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{articleId}/hard")
  public ResponseEntity<?> hardDeleteArticle(@PathVariable("articleId") String articleId) {
    log.info("기사 삭제 요청 시작 {}", articleId);
    articleService.hardDeleteArticle(UUID.fromString(articleId));
    log.info("기사 삭제 요청 완료 {}", articleId);
    return ResponseEntity.noContent().build();
  }
}
