package com.codeit.monew.exception.global;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;

@Getter
public class MoNewException extends RuntimeException {

  final LocalDateTime timestamp;
  final ErrorCode errorCode;
  final Map<String, Object> details;

  public MoNewException(LocalDateTime timestamp, ErrorCode errorCode, Map<String, Object> details) {
    this.timestamp = timestamp;
    this.errorCode = errorCode;
    this.details = details;
  }
}
