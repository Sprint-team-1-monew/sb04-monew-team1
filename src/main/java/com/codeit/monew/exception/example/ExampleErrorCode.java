package com.codeit.monew.exception.example;

import com.codeit.monew.exception.global.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExampleErrorCode implements ErrorCode {

  EXAMPLE_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND.value(), "예외 예시 입니다.");
  private final int status;
  private final String message;

  ExampleErrorCode(int status, String message) {
    this.status = status;
    this.message = message;
  }

  @Override
  public String getName() {
    return name();
  }
}
