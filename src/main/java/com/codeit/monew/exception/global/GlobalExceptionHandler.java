package com.codeit.monew.exception.global;

import com.codeit.monew.exception.example.ExampleException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ExampleException.class)
  public ResponseEntity<String> handleExampleException(ExampleException e) {
    log.error(e.getMessage(), e);
    return ResponseEntity.ok(e.getMessage());
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<?> handleUnexpectedError(RuntimeException ex) {
    return ResponseEntity.internalServerError().body(Map.of("error", "예상치 못한 오류가 발생했습니다."));
  }
}