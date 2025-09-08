package com.codeit.monew.exception.article;

import com.codeit.monew.exception.global.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ArticleErrorCode implements ErrorCode {

  ARTICLE_DUPLICATE_EXCEPTION(HttpStatus.CONFLICT.value(), "중복된 기사입니다."),
  ARTICLE_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND.value(), "기사를 찾을 수 없습니다.");
  private final int status;
  private final String message;

  ArticleErrorCode(int status, String message) {
    this.status = status;
    this.message = message;
  }

  @Override
  public String getName() {
    return name();
  }
}
