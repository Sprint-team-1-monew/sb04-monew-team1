package com.codeit.monew.comment.response_dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

@Builder
public record CursorPageResponseCommentDto (
  List<CommentListResponse> content,
  String nextCursor,
  LocalDateTime nextAfter,
  Integer size,
  Long totalElements,
  Boolean hasNext
){}
