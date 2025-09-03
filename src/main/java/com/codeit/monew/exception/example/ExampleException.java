package com.codeit.monew.exception.example;


import com.codeit.monew.exception.global.ErrorCode;
import com.codeit.monew.exception.global.MoNewException;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;

@Getter
public class ExampleException extends MoNewException {

  public ExampleException(LocalDateTime timestamp, ErrorCode errorCode,
      Map<String, Object> details) {
    super(timestamp, errorCode, details);
  }
}
