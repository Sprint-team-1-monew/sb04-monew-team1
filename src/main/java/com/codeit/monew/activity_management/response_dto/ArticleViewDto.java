package com.codeit.monew.activity_management.response_dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleViewDto(
  UUID id,
  UUID viewedBy,
  LocalDateTime createdAt,
  String source,
  String sourceUrl,
  String articleTitle,
  LocalDateTime articlePublishDate,
  String articleSummary,
  long articleCommentCount,
  long articleViewCount
){}