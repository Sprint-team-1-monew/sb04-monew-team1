package com.codeit.monew.comment.controller;

import com.codeit.monew.comment.entity.CommentOrderBy;
import com.codeit.monew.comment.entity.SortDirection;
import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.request.CommentUpdateRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import com.codeit.monew.comment.response_dto.CommentLikeDto;
import com.codeit.monew.comment.response_dto.CursorPageResponseCommentDto;
import com.codeit.monew.comment.service.CommentService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    log.debug("댓글 등록 응답 : {}", create.id());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(create);
  }

  @PatchMapping(path = "/{commentId}")
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

  @GetMapping
  public ResponseEntity<CursorPageResponseCommentDto> getAllComments(@RequestParam UUID articleId,
      @RequestParam CommentOrderBy orderBy,
      @RequestParam SortDirection direction,
      @RequestParam (required = false) String cursor,
      @RequestParam (required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime after,
      @RequestParam (defaultValue = "50") int limit,
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId) {

    log.info("댓글 목록 조회 test : {}", requestUserId);
    CursorPageResponseCommentDto responseCommentDto= commentService.getComments(articleId, orderBy, direction, cursor, after, limit, requestUserId);
    log.info("댓글 목록 조회 test 종료 : {}", responseCommentDto);

    return ResponseEntity.status(HttpStatus.OK).body(responseCommentDto);
  }

  @PostMapping(path = "/{commentId}/comment-likes")
  public ResponseEntity<CommentLikeDto> commentLike(@PathVariable("commentId") UUID commentId,
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId) {
    log.info("댓글 좋아요 등록 요청 : {}", commentId);

    CommentLikeDto commentLikeDto = commentService.commentLike(commentId, requestUserId);

    log.info("댓글 좋아요 등록 요청 완료 : {}", commentLikeDto);
    return ResponseEntity.status(HttpStatus.OK).body(commentLikeDto);
  }

  @DeleteMapping(path = "/{commentId}/comment-likes")
  public ResponseEntity<Void> deleteCommentLike(@PathVariable("commentId") UUID commentId,
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId) {

    commentService.deleteCommentLike(commentId, requestUserId);

    return ResponseEntity.noContent().build();
  }


}
