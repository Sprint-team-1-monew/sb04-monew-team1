package com.codeit.monew.article.response_dto;

import java.time.LocalDateTime;
import java.util.ArrayList;

public record ArticleRestoreResultDto(
  LocalDateTime restoreDate,
  ArrayList<String> restoredArticleIds,
  long restoredArticleCount
)
{}