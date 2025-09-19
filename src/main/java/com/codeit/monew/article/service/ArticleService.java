package com.codeit.monew.article.service;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticlesViewUser;
import com.codeit.monew.article.mapper.ArticleMapper;
import com.codeit.monew.article.mapper.ArticleViewMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.repository.ArticleViewUserRepository;
import com.codeit.monew.article.response_dto.ArticleDto;
import com.codeit.monew.article.response_dto.ArticleViewDto;
import com.codeit.monew.article.response_dto.CursorPageResponseArticleDto;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.comment.repository.likeRepository.CommentLikeRepository;
import com.codeit.monew.exception.article.ArticleNotFoundException;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.exception.UserErrorCode;
import com.codeit.monew.user.exception.UserException;
import com.codeit.monew.user.repository.UserRepository;
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
import org.springframework.dao.DataIntegrityViolationException;
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
  private final ArticleViewMapper articleViewMapper;
  private final CommentRepository commentRepository;
  private final CommentLikeRepository commentLikeRepository;

  public void softDeleteArticle(UUID articleId) {
    log.info("기사 논리 삭제 시작 {}", articleId);
    Article checkedArticle =  checkArticle(articleId);
    checkedArticle.setDeleted(true);
    articleRepository.save(checkedArticle);
    log.info("기사 논리 삭제 완료 {}", articleId);
  }

  public void hardDeleteArticle(UUID articleId)  {
    log.info("기사 물리 삭제 시작 {}", articleId);
    Article checkedArticle = checkArticle(articleId);

    List<Comment> comments = commentRepository.findByArticle(checkedArticle);
    comments.forEach(c -> {
      commentLikeRepository.deleteByCommentId(c.getId()); // 손자(좋아요) 먼저
      commentRepository.delete(c);                        // 자식(댓글) 삭제
    });
    articleViewUserRepository.deleteByArticle(checkedArticle);

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

    limit = 50; // 50으로 강제 고정

    List<Article> articles = articleRepository.searchArticles(keyword, interestId, sourceIn, publishDateFrom, publishDateTo, orderBy, direction, cursor, after, limit);
    List<ArticleDto> content = new ArrayList<>();
    Optional<User> user = userRepository.findById(requestUserId); // npe 날 수 있으니 예외 처리, 충돌을 우려하며 나중에 유저 쪽 코드랑 머지 되었을 때 예외 처리 추가

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

    if (articles.size() > limit) {
      articles = articles.subList(0, limit);
    }

    for (Article article : articles) {
      boolean viewed = articleViewUserRepository.existsByArticleAndUser(article, user.get());
      ArticleDto dto = articleMapper.toDto(article, viewed);
      content.add(dto);
    }

    CursorPageResponseArticleDto responseArticleDto = new CursorPageResponseArticleDto(content, nextCursor, nextAfter, size, totalElements, hasNex);

    log.info("뉴스 기사 검색 완료");
    return responseArticleDto;
  }

  public ArticleViewDto getArticleViewDto(UUID articleId, UUID requestUserId) {
    log.info("기사 뷰 DTO 조회 시작 - articleId={}, requestUserId={}", articleId, requestUserId);
    Article article = checkArticle(articleId);
    User user = checkUser(requestUserId);

    log.info("기사 뷰 기록 조회 시작 - articleId={}, userId={}", articleId, requestUserId);
    ArticlesViewUser viewUser = articleViewUserRepository.findByArticleAndUser(article, user)
        .orElseGet(() -> createViewUserSafely(article, user));

    ArticleViewDto dto = articleViewMapper.toDto(
      article, user.getId(), viewUser.getCreatedAt()
    );

    updateArticleView(article);

    log.info("기사 뷰 DTO 조회 완료 - articleId={}, requestUserId={}, viewedAt={}",
        articleId, requestUserId, viewUser.getCreatedAt());

    return dto;
  }

  public List<String> findSources(){
    return articleRepository.findDistinctSources();
  }

  private ArticlesViewUser createViewUserSafely(Article article, User user) {
    try {
      ArticlesViewUser saved = articleViewUserRepository.save(new ArticlesViewUser(article, user));
      articleViewUserRepository.flush(); // 감사필드(createdAt) 채워짐 보장
      log.info("기사 뷰 기록 생성 - viewId={}, viewedAt={}", saved.getId(), saved.getCreatedAt());
      return saved;
    } catch (DataIntegrityViolationException ex) {
      // 다른 쓰레드가 동시에 먼저 삽입 → 기존 레코드 재조회
      ArticlesViewUser existing = articleViewUserRepository
          .findByArticleAndUser(article, user)
          .orElseThrow(() -> ex); // 정말 다른 예외면 그대로 던짐
      log.info("동시성 충돌 감지: 기존 뷰 기록 사용 - viewId={}, viewedAt={}",
          existing.getId(), existing.getCreatedAt());
      return existing;
    }
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

  private User checkUser(UUID userId) {
    log.info("유저 존재 검증 시작 {}", userId);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.info("유저가 없습니다 {}", userId);
          Map<String, Object> details = new HashMap<>();
          details.put("이유", userId + " 유저는 존재하지 않습니다.");
          return new UserException(UserErrorCode.USER_NOT_FOUND, details); // 유저 낫 파운드 이셉션으로 변경하기
        });
    log.info("유저 존재 검증 완료 {}", userId);
    return user;
  }

  private void updateArticleView(Article article){
    int articleViewCount = articleViewUserRepository.countByArticle(article);
    article.setArticleViewCount(articleViewCount);

    long commentCount = commentRepository.countByArticleId(article.getId());
    article.setArticleCommentCount(commentCount);

    articleRepository.save(article);
  }
}
