package com.codeit.monew.comment.controller;

import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.request.CommentUpdateRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import com.codeit.monew.comment.service.CommentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

  private final CommentService commentService;

  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<CommentDto> createComment(
      @RequestBody @Valid CommentRegisterRequest commentRegisterRequest) {
    log.info("댓글 등록 요청 : {}", commentRegisterRequest);
    CommentDto create = commentService.createComment(commentRegisterRequest);

    log.debug("댓글 등록 응답 : {}", create);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(create);
  }

  @PatchMapping(path = "/{commentId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<CommentDto> updateComment(
      @PathVariable("commentId") UUID commentId,
      @RequestHeader("Monew-Request-User-ID") UUID userId,
      @RequestBody @Valid CommentUpdateRequest commentUpdateRequest) {
    log.info("댓글 수정 요청 : {}", commentUpdateRequest);

    CommentDto update = commentService.updateComment(commentId, userId, commentUpdateRequest);

    log.debug("댓글 수정 응답 : {}", update);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(update);
  }


  @DeleteMapping(path = "/{commentId}")
  public ResponseEntity<Void> deleteCommentSoft(
      @PathVariable("commentId") UUID commentId
  ) {
    commentService.softDeleteComment(commentId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping(path = "/{commentId}/hard")
  public ResponseEntity<Void> deleteCommentHard(
      @PathVariable("commentId") UUID commentId
  ) {
    commentService.hardDeleteComment(commentId);
    return ResponseEntity.noContent().build();
  }


}
