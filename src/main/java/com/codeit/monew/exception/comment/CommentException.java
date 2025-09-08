package com.codeit.monew.exception.comment;

import com.codeit.monew.exception.global.ErrorCode;
import com.codeit.monew.exception.global.MoNewException;
import java.time.LocalDateTime;
import java.util.Map;

public class CommentException extends MoNewException {

  public CommentException(LocalDateTime timestamp, ErrorCode errorCode,
      Map<String, Object> details) {
    super(timestamp, errorCode, details);
  }
}
