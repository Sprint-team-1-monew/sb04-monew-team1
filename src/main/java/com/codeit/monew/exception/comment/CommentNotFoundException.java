package com.codeit.monew.exception.comment;

import com.codeit.monew.exception.example.ExampleErrorCode;
import com.codeit.monew.exception.global.ErrorCode;
import java.time.LocalDateTime;
import java.util.Map;

public class CommentNotFoundException extends CommentException {

  public CommentNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(LocalDateTime.now(), errorCode, details);
  }
}
