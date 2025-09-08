package com.codeit.monew.exception.interest;

import com.codeit.monew.exception.global.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum InterestErrorCode implements ErrorCode {

  INTEREST_NAME_DUPLICATION(HttpStatus.CONFLICT.value(),"유사한 이름의 관심사가 이미 존재합니다."),

  INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "관심사를 찾을 수 없습니다.");

  private final int status;
  private final String message;

  InterestErrorCode(int status, String message) {
    this.status = status;
    this.message = message;
  }

  @Override
  public String getName() {
    return name();
  }
}
