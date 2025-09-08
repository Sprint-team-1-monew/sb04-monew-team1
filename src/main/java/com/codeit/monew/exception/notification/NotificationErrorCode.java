package com.codeit.monew.exception.notification;

import com.codeit.monew.exception.global.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum NotificationErrorCode implements ErrorCode {

  NOT_OWNER(HttpStatus.FORBIDDEN.value(), "올바르지 않은 사용자"),
  NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "사용자 정보 없음"),
  INVALID_REQUEST(HttpStatus.BAD_REQUEST.value(), "잘못된 요청"),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류");

  private final int status;
  private final String message;

  NotificationErrorCode(int status, String message) {
    this.status = status;
    this.message = message;
  }

  @Override
  public String getName() {
    return name();
  }
}
