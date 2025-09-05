package com.codeit.monew.exception.article;


import com.codeit.monew.exception.global.ErrorCode;
import com.codeit.monew.exception.global.MoNewException;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;

@Getter
public class ArticleException extends MoNewException {

  public ArticleException(LocalDateTime timestamp, ErrorCode errorCode,
      Map<String, Object> details) {
    super(timestamp, errorCode, details);
  }
}
