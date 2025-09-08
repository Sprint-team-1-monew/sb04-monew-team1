package com.codeit.monew.exception.comment;

import com.codeit.monew.exception.example.ExampleErrorCode;
import com.codeit.monew.exception.global.ErrorCode;
import java.time.LocalDateTime;
import java.util.Map;

public class CommentNotFoundException extends CommentException {

  public CommentNotFoundException(Map<String, Object> details) {
    super(LocalDateTime.now(), ExampleErrorCode.EXAMPLE_NOT_FOUND_EXCEPTION, details);
  }
}
