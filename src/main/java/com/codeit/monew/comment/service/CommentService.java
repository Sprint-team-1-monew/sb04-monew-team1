package com.codeit.monew.comment.service;

import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.request.CommentUpdateRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import com.codeit.monew.exception.comment.CommentNotFoundException;
import com.codeit.monew.user.repository.UserRepository;
import com.codeit.monew.user.service.UserService;
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

  private final CommentRepository commentRepository;
  private final CommentMapper commentMapper;
  private final UserRepository userRepository;
  private final ArticleRepository articleRepository;

  public CommentDto createComment(CommentRegisterRequest commentRegisterRequest) {

    Comment commentEntity = commentMapper.toCommentEntity(commentRegisterRequest);

    if(!userRepository.existsById(commentEntity.getUser().getId())) {
      throw new IllegalArgumentException("사용자 ID가 존재하지 않습니다.");
    }

    if(!articleRepository.existsById(commentEntity.getArticle().getId())) {
      throw new IllegalArgumentException("기사가 존재하지 않습니다.");
    }


    try {
      Comment saved = commentRepository.save(commentEntity);
      log.info("댓글 등록 완료 : {}", saved);
      //응답 DTO 변환
      return commentMapper.toCommentDto(saved);
    } catch (Exception e) {
      log.error("댓글 저장 중 오류 발생: {}", e.getMessage());
      throw e;
    }
  }

  public CommentDto updateComment(UUID commentId, UUID userId,
      CommentUpdateRequest commentUpdateRequest) {

    //댓글 유효 검증
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException(Map.of(
            "commentId", commentId,
            "message", "댓글이 존재하지 않습니다."
        )));

    //사용자 본인 인지 검증
    if (!comment.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("본인만 수정할 수 있습니다.");
    }

    //댓글 업데이트
    comment.updateContent(commentUpdateRequest.content());
    commentRepository.save(comment);

    return commentMapper.toCommentDto(comment);
  }

}
