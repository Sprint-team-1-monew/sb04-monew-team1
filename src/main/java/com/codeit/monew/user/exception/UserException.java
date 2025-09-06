package com.codeit.monew.user.exception;

import com.codeit.monew.exception.global.ErrorCode;
import com.codeit.monew.exception.global.MoNewException;
import java.time.LocalDateTime;
import java.util.Map;

public class UserException extends MoNewException {
  public UserException(ErrorCode errorCode, Map<String, Object> details) {
    super(LocalDateTime.now(), errorCode, details);
  }
}
