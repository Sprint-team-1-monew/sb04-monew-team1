package com.codeit.monew.exception.notification;

import com.codeit.monew.exception.global.ErrorCode;
import com.codeit.monew.exception.global.MoNewException;
import java.time.LocalDateTime;
import java.util.Map;

public class NotificationException extends MoNewException {

  public NotificationException(ErrorCode errorCode, Map<String, Object> details) {
    super(LocalDateTime.now(), errorCode, details);
  }
}
