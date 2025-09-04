package com.codeit.monew.comment.request;

import java.util.UUID;

public record CommentRegisterRequest(
  UUID articleId,
  UUID userId,
  String content
){}