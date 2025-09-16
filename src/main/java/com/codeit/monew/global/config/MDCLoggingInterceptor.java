package com.codeit.monew.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

public class MDCLoggingInterceptor implements HandlerInterceptor {

  public static final String REQUEST_ID = "requestId";
  public static final String REQUEST_HTTP_METHOD = "requestHttpMethod";
  public static final String REQUEST_PATH = "requestPath";

  public static final String HEADER = "Discodeit-Request_ID";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String requestId = UUID.randomUUID().toString().replace("-","");

    MDC.put(REQUEST_ID, requestId);
    MDC.put(REQUEST_HTTP_METHOD, request.getMethod());
    MDC.put(REQUEST_PATH, request.getRequestURI());

    response.setHeader(REQUEST_ID, requestId);

    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    MDC.clear();
  }

}
