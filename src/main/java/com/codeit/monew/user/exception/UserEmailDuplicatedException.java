package com.codeit.monew.user.exception;

import com.codeit.monew.exception.global.ErrorCode;
import com.codeit.monew.exception.global.MoNewException;
import java.time.LocalDateTime;
import java.util.Map;

public class UserEmailDuplicatedException extends MoNewException {
  public UserEmailDuplicatedException(ErrorCode errorCode, String email) {
    super(LocalDateTime.now(), errorCode, Map.of("email", email));
  }
}
