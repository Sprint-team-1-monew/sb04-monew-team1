package com.codeit.monew.user.exception;

import com.codeit.monew.exception.global.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

  USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "회원이 존재하지 않습니다."),
  USER_EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST.value(), "이메일이 중복되었습니다.");

  private final int status;
  private final String message;

  @Override
  public String getName() {
    return this.name();
  }

}

