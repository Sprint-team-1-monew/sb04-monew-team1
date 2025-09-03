package com.codeit.monew.exception.global;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
    LocalDateTime timestamp,
    String code,
    String message,
    Map<String, Object> details,
    String exceptionType, //발생한 예외 클래스의 이름
    int status //HTTP 상태코드
) {

}
