package com.codeit.monew.comment.service;

import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.request.CommentUpdateRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import com.codeit.monew.user.entity.User;
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

  public CommentDto createComment (CommentRegisterRequest commentRegisterRequest) {

    Comment commentEntity = commentMapper.toCommentEntity(commentRegisterRequest);

    //userRepository 연동되면 삭제
    if (!userExists(commentEntity.getUser().getId())) {
      throw new IllegalArgumentException("사용자 ID가 존재하지 않습니다.");
    }
    //articleRepository 연동되면 삭제
    if (!articleExists(commentEntity.getArticle().getId())) {
      throw new IllegalArgumentException("기사가 존재하지 않습니다.");
    }

    //사용자 존재 확인
//    if(!userRepository.existsById(commentEntity.getUser().getId())) {
//      throw new IllegalArgumentException("사용자 ID가 존재하지 않습니다.");
//    }
    //기사 존재 확인
//    if(!articleRepository.existsById(commentEntity.getArticle().getId())) {
//      throw new IllegalArgumentException("기사가 존재하지 않습니다.");
//    }
    //저장

    try {
      Comment saved = commentRepository.save(commentEntity);
      log.info("댓글 등록 완료 : {}", saved);
      //응답 DTO 변환
      return commentMapper.toCommentDto(saved);
    }catch (Exception e) {
      log.error("댓글 저장 중 오류 발생: {}", e.getMessage());
      throw e;
    }
  }

  private boolean userExists(UUID userId) {
    // TODO: userRepository 연결되면 실제 체크
    return true;
  }

  private boolean articleExists(UUID articleId) {
    // TODO: articleRepository 연결되면 실제 체크
    return true;
  }

  public CommentDto updateComment (CommentUpdateRequest commentUpdateRequest) {
    Comment commentEntity = commentMapper.toCommentEntity(commentUpdateRequest);

    return commentMapper.toCommentDto(commentRepository.save(commentEntity));
  }

}
