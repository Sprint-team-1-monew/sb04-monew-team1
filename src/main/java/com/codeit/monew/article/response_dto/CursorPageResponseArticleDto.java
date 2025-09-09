package com.codeit.monew.article.response_dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CursorPageResponseArticleDto(
  List<ArticleDto> content,
  String nextCursor,
  LocalDateTime nextAfter,
  int size,
  long totalElements,
  boolean hasNext
)
{}