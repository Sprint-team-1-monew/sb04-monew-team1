package com.codeit.monew.exception.subscription;

import com.codeit.monew.exception.global.ErrorCode;
import com.codeit.monew.exception.global.MoNewException;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;

@Getter
public class SubscriptionException extends MoNewException {

  public SubscriptionException(ErrorCode errorCode, Map<String, Object> details) {
    super(LocalDateTime.now(), errorCode, details);
  }
}
