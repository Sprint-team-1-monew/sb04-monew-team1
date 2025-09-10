package com.codeit.monew.exception.comment;

import com.codeit.monew.exception.global.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommentErrorCode implements ErrorCode {

  COMMENT_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND.value(), "댓글을 찾을 수 없습니다."),
  IDENTITY_VERIFICATION_EXCEPTION(HttpStatus.BAD_REQUEST.value(), "댓글을 등록한 사용자가 아닙니다."),

  DUPLICATE_LIKES(HttpStatus.BAD_REQUEST.value(), "이미 좋아요를 누른 댓글입니다."),
  COMMENT_NOT_LIKE(HttpStatus.BAD_REQUEST.value(), "좋아요를 누르지 않았습니다."),;

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
