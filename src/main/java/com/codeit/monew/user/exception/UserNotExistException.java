package com.codeit.monew.user.exception;

import com.codeit.monew.exception.global.MoNewException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class UserNotExistException extends MoNewException {

  public UserNotExistException(UserErrorCode userErrorCode, UUID userId) {
    super(LocalDateTime.now(), userErrorCode, Map.of("userId", userId));
  }
}
