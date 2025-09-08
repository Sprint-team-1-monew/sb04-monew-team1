package com.codeit.monew.exception.global;

import com.codeit.monew.exception.article.ArticleException;
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
  public ResponseEntity<ErrorResponse> handleExampleException(ExampleException e) {
    ErrorResponse errorResponse = new ErrorResponse(
        e.getTimestamp(),
        e.getErrorCode().getName(),
        e.getErrorCode().getMessage(),
        e.getDetails(),
        e.getClass().getSimpleName(),
        e.getErrorCode().getStatus()
    );
    return ResponseEntity.status(errorResponse.status()).body(errorResponse);
  }

  @ExceptionHandler(MoNewException.class)
  public ResponseEntity<ErrorResponse> handleMoNewException(MoNewException e) {
    log.error(e.getMessage(), e);

    ErrorResponse errorResponse = new ErrorResponse(e.getTimestamp(), e.getErrorCode().getName(),
        e.getErrorCode().getMessage(), e.getDetails(), e.getClass().getSimpleName(),
        e.getErrorCode().getStatus());

    return ResponseEntity.status(e.getErrorCode().getStatus()).body(errorResponse);
  }
  
  @ExceptionHandler(ArticleException.class)
  public ResponseEntity<ErrorResponse> handleArticleException(ArticleException e) {
    ErrorResponse errorResponse = new ErrorResponse(
        e.getTimestamp(),
        e.getErrorCode().getName(),
        e.getErrorCode().getMessage(),
        e.getDetails(),
        e.getClass().getSimpleName(),
        e.getErrorCode().getStatus()
    );
    return ResponseEntity.status(errorResponse.status()).body(errorResponse);

  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<?> handleUnexpectedError(RuntimeException ex) {
    return ResponseEntity.internalServerError().body(Map.of("error", "예상치 못한 오류가 발생했습니다."));
  }
}
