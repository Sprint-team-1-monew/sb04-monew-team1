package com.codeit.monew.exception.article;

import java.time.LocalDateTime;
import java.util.Map;

public class ArticleNotFoundException extends ArticleException {

  public ArticleNotFoundException(Map<String, Object> details) {
    super(LocalDateTime.now(), ArticleErrorCode.ARTICLE_NOT_FOUND_EXCEPTION, details);
  }
}
