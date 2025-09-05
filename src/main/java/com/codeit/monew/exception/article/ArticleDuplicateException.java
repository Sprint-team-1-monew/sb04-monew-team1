package com.codeit.monew.exception.article;

import java.time.LocalDateTime;
import java.util.Map;

public class ArticleDuplicateException extends ArticleException {

  public ArticleDuplicateException(Map<String, Object> details) {
    super(LocalDateTime.now(), ArticleErrorCode.ARTICLE_DUPLICATE_EXCEPTION, details);
  }
}
