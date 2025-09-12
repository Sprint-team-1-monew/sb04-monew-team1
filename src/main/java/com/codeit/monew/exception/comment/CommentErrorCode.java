package com.codeit.monew.exception.comment;

import com.codeit.monew.exception.global.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
public enum CommentErrorCode implements ErrorCode {

  COMMENT_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND.value(), "댓글을 찾을 수 없습니다."),
  IDENTITY_VERIFICATION_EXCEPTION(HttpStatus.BAD_REQUEST.value(), "댓글을 등록한 사용자가 아닙니다."),;

  private final int status;
  private final String message;

  CommentErrorCode(int Status, String message) {
    this.status = Status;
    this.message = message;
  }

  @Override
  public String getName() {
    return name();
  }
}
