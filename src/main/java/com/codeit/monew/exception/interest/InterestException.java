package com.codeit.monew.exception.interest;

import com.codeit.monew.exception.global.ErrorCode;
import com.codeit.monew.exception.global.MoNewException;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;

@Getter
public class InterestException extends MoNewException {

  public InterestException(ErrorCode errorCode, Map<String, Object> details) {
    super(LocalDateTime.now(), errorCode, details);
  }
}