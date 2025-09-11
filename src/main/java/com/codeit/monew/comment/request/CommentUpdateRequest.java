package com.codeit.monew.comment.request;

import jakarta.validation.constraints.NotNull;

public record CommentUpdateRequest(

  @NotNull(message = "수정 할 내용은 필수 입니다.")
  String content
){}
