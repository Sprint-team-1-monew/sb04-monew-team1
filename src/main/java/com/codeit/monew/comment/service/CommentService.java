package com.codeit.monew.comment.service;

import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import com.codeit.monew.comment.entity.CommentOrderBy;
import com.codeit.monew.comment.entity.SortDirection;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.comment.repository.likeRepository.CommentLikeQuerydslRepositoryCustom;
import com.codeit.monew.comment.repository.likeRepository.CommentLikeRepository;
import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.request.CommentUpdateRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import com.codeit.monew.comment.response_dto.CommentLikeDto;
import com.codeit.monew.comment.response_dto.CursorPageResponseCommentDto;
import com.codeit.monew.exception.article.ArticleNotFoundException;
import com.codeit.monew.exception.comment.CommentErrorCode;
import com.codeit.monew.exception.comment.CommentException;
import com.codeit.monew.notification.event.CommentLikeEvent;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.exception.UserErrorCode;
import com.codeit.monew.user.exception.UserException;
import com.codeit.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentService {

  private final CommentMapper commentMapper;
  private final CommentRepository commentRepository;
  private final CommentLikeQuerydslRepositoryCustom commentLikeQuerydslRepositoryCustom;
  private final CommentLikeRepository commentLikeRepository;

  private final UserRepository userRepository;
  private final ArticleRepository articleRepository;

  private final ApplicationEventPublisher publisher;

  public CommentDto createComment(CommentRegisterRequest commentRegisterRequest) {

    log.info("댓글 등록 시작 : {}", commentRegisterRequest);
    Comment commentEntity = commentMapper.toCommentEntity(commentRegisterRequest);

    User user = userRepository.findById(commentRegisterRequest.userId())
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND,
            Map.of("userId", commentRegisterRequest.userId())));

    Article article = articleRepository.findById(commentRegisterRequest.articleId())
        .orElseThrow(() -> new ArticleNotFoundException(
            Map.of("articleId", commentRegisterRequest.articleId())));

    commentEntity.setUser(user);
    commentEntity.setArticle(article);

    Comment saved = commentRepository.save(commentEntity);

    CommentDto dto = commentMapper.toCommentDto(saved, false);
    log.info("CommentDto 반환 전 : {}", dto); // <- 이걸 추가
    //응답 DTO 변환
//    return commentMapper.toCommentDto(saved);

    return dto;
  }


  public CommentDto updateComment(UUID commentId, UUID userId,
      CommentUpdateRequest commentUpdateRequest) {
    log.info("댓글 수정 시작 : {}", commentId);
    //댓글 유효 검증
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND_EXCEPTION,
            Map.of("commentId", commentId)));

    //사용자 본인 인지 검증
    if (!comment.getUser().getId().equals(userId)) {
      throw new CommentException(CommentErrorCode.IDENTITY_VERIFICATION_EXCEPTION,
          Map.of("userId", userId));
    }

    //댓글 업데이트
    comment.updateContent(commentUpdateRequest.content());
    Comment saved = commentRepository.save(comment);
    log.info("댓글 수정 완료 : {}", commentId);

    boolean liked = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);

    return commentMapper.toCommentDto(saved, liked);
  }

  public void softDeleteComment(UUID commentId) {
    log.info("댓글 논리 삭제 시작 : {}", commentId);
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND_EXCEPTION,
            Map.of("commentId", commentId)));

    comment.setIsDeleted(true);
    comment.setDeletedAt(LocalDateTime.now());

    commentRepository.save(comment);

    log.info("댓글 논리 삭제 완료 : {}", comment);

  }

  public void hardDeleteComment(UUID commentId) {
    log.info("댓글 물리 삭제 시작 : {}", commentId);
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND_EXCEPTION,
            Map.of("commentId", commentId)));

    commentLikeQuerydslRepositoryCustom.deleteByCommentId(commentId);

    commentRepository.delete(comment);
    log.info("댓글 물리 삭제 완료 : {}", commentId);
  }

  public CursorPageResponseCommentDto getComments(UUID articleId,
      CommentOrderBy orderBy,
      SortDirection direction,
      String cursor,
      LocalDateTime after,
      int limit,
      UUID requestUserId) {

    log.info(
        "댓글 목록 조회 시작 - articleID = {}, orderBy = {}, direction = {}, cursor = {}, after = {}, limit = {}, requestUserId = {}",
        articleId, orderBy, direction, cursor, after, limit, requestUserId);

    // 첫 페이지 조회 시 cursor/after 처리
    LocalDateTime effectiveAfter = after;
    if (cursor == null || cursor.isEmpty()) {
      effectiveAfter = null;
    }

    List<Comment> comments = commentRepository.findComments(articleId, orderBy, direction,
        cursor, effectiveAfter, limit);

    List<CommentDto> contents = comments.stream()
        .filter(comment -> !comment.getIsDeleted())
        .map(comment -> {
          boolean liked = commentLikeRepository.existsByCommentIdAndUserId(comment.getId(),
              requestUserId);
          return commentMapper.toCommentDto(comment, liked);
        })
        .toList();

    log.info(
        "댓글 목록 조회 완료 - articleID = {}, orderBy = {}, direction = {}, cursor = {}, after = {}, limit = {}, requestUserId = {}",
        articleId, orderBy, direction, cursor, after, limit, requestUserId);

    String nextCursor = null;
    LocalDateTime nextAfter = null;
    Integer size = contents.size();
    Long totalElements = commentRepository.countByArticleId(articleId);
    Boolean hasNext = comments.size() == limit + 1;

    if (hasNext) {
      nextAfter = comments.get(limit).getCreatedAt();
      nextCursor = nextAfter.toString();
    }

    return CursorPageResponseCommentDto.builder()
        .content(contents)
        .nextCursor(nextCursor)
        .nextAfter(nextAfter)
        .size(size)
        .totalElements(totalElements)
        .hasNext(hasNext)
        .build();

  }

  public CommentLikeDto commentLike(UUID commentId, UUID requestUserId) {

    log.debug("댓글 좋아요 등록 시작 - 댓글 아이디 : {} , 좋아요 등록 요청자 아이디 : {}", commentId, requestUserId);
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND_EXCEPTION,
            Map.of("commentId", commentId)));

    User user = userRepository.findById(requestUserId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND,
            Map.of("requestUserId", requestUserId)));

    if (commentLikeRepository.existsByCommentIdAndUserId(commentId, requestUserId)) {
      throw new CommentException(CommentErrorCode.DUPLICATE_LIKES,
          Map.of("commentId", commentId, "requestUserId", requestUserId));
    }

    CommentLike commentLike = CommentLike
        .builder()
        .comment(comment)
        .user(user)
        .build();

    commentLikeRepository.save(commentLike);

    // 좋아요 카운트 증가
    comment.setLikeCount(comment.getLikeCount() + 1);
    commentRepository.save(comment);

    publisher.publishEvent(new CommentLikeEvent(comment.getUser().getId(),comment.getId(),requestUserId));

    log.debug("댓글 좋아요 등록 완료 - 댓글 아이디 : {} , 좋아요 등록 요청자 아이디 : {}", commentId, requestUserId);

    return new CommentLikeDto(
        commentLike.getId(),
        user.getId(),
        commentLike.getCreatedAt(),
        comment.getId(),
        comment.getArticle().getId(),
        comment.getUser().getId(),
        comment.getUser().getNickname(),
        comment.getContent(),
        comment.getLikeCount(),
        comment.getCreatedAt()
    );
  }

  public void deleteCommentLike(UUID commentId, UUID requestUserId) {
    log.debug("댓글 좋아요 취소 시작 - 댓글 아이디 : {} , 좋아요 취소 요청자 아이디 : {}", commentId, requestUserId);
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND_EXCEPTION,
            Map.of("commentId", commentId)));

    User user = userRepository.findById(requestUserId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND,
            Map.of("requestUserId", requestUserId)));

    CommentLike commentLike = commentLikeRepository.findByCommentAndUser(comment, user)
        .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_LIKE,
            Map.of("commentId", commentId)));

    commentLikeRepository.delete(commentLike);

    comment.setLikeCount(comment.getLikeCount() - 1);
    commentRepository.save(comment);

    log.debug("댓글 좋아요 취소 완료 - 댓글 아이디 : {} , 좋아요 취소 요청자 아이디 : {}", commentId, requestUserId);
  }
}
