package com.codeit.monew.comment.response_dto;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponseCommentDto (
  List<CommentDto> content,
  String nextCursor,
  LocalDateTime nextAfter,
  Integer size,
  Long totalElements,
  Boolean hasNext
){}