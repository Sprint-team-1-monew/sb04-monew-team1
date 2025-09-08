package com.codeit.monew.exception.comment;

import com.codeit.monew.exception.global.ErrorCode;
import com.codeit.monew.exception.global.MoNewException;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.cglib.core.Local;

public class CommentException extends MoNewException {

  public CommentException(ErrorCode errorCode,
      Map<String, Object> details) {
    super(LocalDateTime.now(), errorCode, details);
  }
}
