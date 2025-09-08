package com.codeit.monew.exception.comment;

import com.codeit.monew.exception.global.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
public enum CommentErrorCode implements ErrorCode {

  USER_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "유저를 찾을 수 없습니다."),
  ARTICLE_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "기사를 찾을 수 없습니다."),
  COMMENT_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "댓글을 찾을 수 없습니다.");

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
