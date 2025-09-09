package com.codeit.monew.article.controller;


import com.codeit.monew.article.response_dto.CursorPageResponseArticleDto;
import com.codeit.monew.article.service.ArticleService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
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

  @GetMapping("/api/articles")
  public ResponseEntity<CursorPageResponseArticleDto> getArticles(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) UUID interestId,
      @RequestParam(required = false) List<String> sourceIn,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime publishDateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime publishDateTo,
      @RequestParam String orderBy,
      @RequestParam String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @RequestParam int limit,
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId
  ) {
    log.info("[GET /api/articles] 요청 시작");
    log.info("keyword: {}, interestId: {}, sourceIn: {}, publishDateFrom: {}, publishDateTo: {}, orderBy: {}, direction: {}, cursor: {}, after: {}, limit: {}, requestUserId: {}",
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo, orderBy, direction, cursor, after, limit, requestUserId);

    CursorPageResponseArticleDto response = articleService.articleSearch(
        keyword,
        interestId,
        sourceIn,
        publishDateFrom,
        publishDateTo,
        orderBy,
        direction,
        cursor,
        after,
        limit,
        requestUserId
    );

    // 응답 로그
    log.info("[GET /api/articles] 요청 완료 - 결과 size: {}, hasNext: {}, nextCursor: {}",
        response.content().size(),
        response.hasNext(),
        response.nextCursor());

    return ResponseEntity.ok(response);
  }
}
