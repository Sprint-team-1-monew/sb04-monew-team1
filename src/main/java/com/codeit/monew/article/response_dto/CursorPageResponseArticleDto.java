package com.codeit.monew.article.response_dto;

import java.time.LocalDateTime;
import java.util.ArrayList;

public record CursorPageResponseArticleDto(
  ArrayList<ArticleDto> content,
  String nextCursor,
  LocalDateTime nextAfter,
  int size,
  long totalElements,
  boolean hasNext
)
{}