package com.codeit.monew.comment.controller;

import com.codeit.monew.comment.request.CommentRegisterRequest;
import com.codeit.monew.comment.response_dto.CommentDto;
import com.codeit.monew.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

  private CommentService commentService;

  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<CommentDto> createComment(
      @RequestBody CommentRegisterRequest commentRegisterRequest) {

    CommentDto create = commentService.createComment(commentRegisterRequest);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(create);
  }
}
