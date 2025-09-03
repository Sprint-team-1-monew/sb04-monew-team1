package com.codeit.monew.exception.example;

import java.time.LocalDateTime;
import java.util.Map;

public class ExampleNotFoundException extends ExampleException {

  public ExampleNotFoundException(Map<String, Object> details) {
    super(LocalDateTime.now(), ExampleErrorCode.EXAMPLE_NOT_FOUND_EXCEPTION, details);
  }
}
