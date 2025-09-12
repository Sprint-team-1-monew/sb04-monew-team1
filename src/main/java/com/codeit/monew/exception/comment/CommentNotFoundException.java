package com.codeit.monew.exception.comment;

import com.codeit.monew.exception.global.ErrorCode;
import java.util.Map;

public class CommentNotFoundException extends CommentException {

  public CommentNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
