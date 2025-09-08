package com.codeit.monew.comment.service;

import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentLikeQuerydslRepository;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.request.CommentUpdateRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import com.codeit.monew.exception.comment.CommentErrorCode;
import com.codeit.monew.exception.comment.CommentNotFoundException;
import com.codeit.monew.user.repository.UserRepository;
import com.codeit.monew.user.service.UserService;
import java.time.LocalDateTime;
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

  private final UserRepository userRepository;
  private final ArticleRepository articleRepository;

  public CommentDto createComment(CommentRegisterRequest commentRegisterRequest) {

    log.info("댓글 등록 시작 : {}", commentRegisterRequest);
    Comment commentEntity = commentMapper.toCommentEntity(commentRegisterRequest);

    if (!userRepository.existsById(commentEntity.getUser().getId())) {
      throw new IllegalArgumentException("사용자 ID가 존재하지 않습니다.");
    }

    if (!articleRepository.existsById(commentEntity.getArticle().getId())) {
      throw new IllegalArgumentException("기사가 존재하지 않습니다.");
    }

    Comment saved = commentRepository.save(commentEntity);
    log.info("댓글 등록 완료 : {}", saved);
    //응답 DTO 변환
    return commentMapper.toCommentDto(saved);
  }


  public CommentDto updateComment(UUID commentId, UUID userId,
      CommentUpdateRequest commentUpdateRequest) {
    log.info("댓글 수정 시작 : {}", commentId);
    //댓글 유효 검증
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException(CommentErrorCode.COMMENT_NOT_FOUND,
            Map.of("commentId", commentId)));

    //사용자 본인 인지 검증
    if (!comment.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("본인만 수정할 수 있습니다.");
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
        .orElseThrow(() -> new CommentNotFoundException(CommentErrorCode.COMMENT_NOT_FOUND,
            Map.of("commentId", commentId)));

    comment.setIsDeleted(true);
    comment.setDeletedAt(LocalDateTime.now());

    commentRepository.save(comment);
    log.info("댓글 논리 삭제 완료 : {}", comment);

  }

  public void hardDeleteComment(UUID commentId) {
    log.info("댓글 물리 삭제 시작 : {}", commentId);
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException(CommentErrorCode.COMMENT_NOT_FOUND,
            Map.of("commentId", commentId)));

    commentLikeQuerydslRepository.deleteByCommentId(commentId);

    commentRepository.delete(comment);
    log.info("댓글 물리 삭제 완료 : {}", commentId);

  }

}
