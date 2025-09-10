package com.codeit.monew.comment.service;

import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import com.codeit.monew.comment.entity.CommentOrderBy;
import com.codeit.monew.comment.entity.SortDirection;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentLikeQuerydslRepository;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.comment.repository.CommentRepositoryCustom;
import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.request.CommentUpdateRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import com.codeit.monew.comment.response_dto.CommentLikeDto;
import com.codeit.monew.comment.response_dto.CommentListResponse;
import com.codeit.monew.comment.response_dto.CursorPageResponseCommentDto;
import com.codeit.monew.exception.article.ArticleNotFoundException;
import com.codeit.monew.exception.comment.CommentErrorCode;
import com.codeit.monew.exception.comment.CommentException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentService {

  private final CommentMapper commentMapper;
  private final CommentRepository commentRepository;
  private final CommentLikeQuerydslRepository commentLikeQuerydslRepository;
  private final CommentLikeRepository commentLikeRepository;
  private final CommentRepositoryCustom commentRepositoryCustom;

  private final UserRepository userRepository;
  private final ArticleRepository articleRepository;

  public CommentDto createComment(CommentRegisterRequest commentRegisterRequest) {

    log.info("댓글 등록 시작 : {}", commentRegisterRequest);
    Comment commentEntity = commentMapper.toCommentEntity(commentRegisterRequest);

    if (!userRepository.existsById(commentEntity.getUser().getId())) {
      throw new UserException(UserErrorCode.USER_NOT_FOUND,
          Map.of("userId", commentEntity.getUser().getId()));
    }

    if (!articleRepository.existsById(commentEntity.getArticle().getId())) {
      throw new ArticleNotFoundException(Map.of("ArticleId", commentEntity.getArticle().getId()));
    }

    Comment saved = commentRepository.save(commentEntity);

    updateCommentCount(saved.getArticle());

    log.info("댓글 등록 완료 : {}", saved);
    //응답 DTO 변환
    return commentMapper.toCommentDto(saved);
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
    commentRepository.save(comment);
    log.info("댓글 수정 완료 : {}", commentId);

    return commentMapper.toCommentDto(comment);
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
            Map.of( "commentId", commentId)));

    commentLikeQuerydslRepository.deleteByCommentId(commentId);

    commentRepository.delete(comment);
    updateCommentCount(comment.getArticle());
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
        "목록 조회 시작 - articleID = {}, orderBy = {}, direction = {}, cursor = {}, after = {}, limit = {}, requestUserId = {}",
        articleId, orderBy, direction, cursor, after, limit, requestUserId);

    List<Comment> comments = commentRepositoryCustom.findComments(articleId, orderBy, direction,
        cursor, after, limit);

    //DTO 변환
    List<CommentListResponse> content = comments.stream()
        .map(CommentListResponse::fromEntity)
        .toList();

    //커서 값 계산
    String nextCursor = comments.isEmpty() ? null :
        comments.get(comments.size() - 1).getId().toString();

    boolean hasNext = comments.size() == limit; // Limit 만큼 채워졌으면 다음 페이지가 있다고 가정

    return CursorPageResponseCommentDto.builder()
        .content(content)
        .nextCursor(nextCursor)
        .hasNext(hasNext)
        .build();

  }

  public CommentLikeDto commentLike(UUID commentId, UUID requestUserId) {

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
  }

  private void updateCommentCount(Article article){
    int articleCommentCount = commentRepository.countByArticle(article);
    article.setArticleCommentCount(articleCommentCount);
    articleRepository.save(article);
  }
}
