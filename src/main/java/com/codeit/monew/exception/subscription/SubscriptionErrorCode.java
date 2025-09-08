package com.codeit.monew.exception.subscription;

import com.codeit.monew.exception.global.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SubscriptionErrorCode implements ErrorCode {

  ALREADY_SUBSCRIBED(HttpStatus.CONFLICT.value(), "이미 구독한 관심사입니다."),
  NOT_SUBSCRIBED(HttpStatus.BAD_REQUEST.value(), "구독하지 않은 관심사입니다.");

  private final int status;
  private final String message;

  SubscriptionErrorCode(int status, String message) {
    this.status = status;
    this.message = message;
  }

  @Override
  public String getName() {
    return name();
  }
}
