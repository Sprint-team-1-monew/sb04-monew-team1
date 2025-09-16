package com.codeit.monew.global.aop;

import com.codeit.monew.global.util.LogParameterFormatter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class MethodLoggingAspect {

  @Before("execution(* com.codeit.monew..service.*.*(..))")
  public void methodCallLogging(JoinPoint joinPoint) {
    String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    Object[] args = joinPoint.getArgs();

    String parameterStr = LogParameterFormatter.getFormattedParameters(args);

    log.info("Method call: (class={}, method={}, parameters=({})", className, methodName, parameterStr);
  }

  @AfterReturning("execution(* com.codeit.monew..service.*.*(..))")
  public void methodReturnLogging(JoinPoint joinPoint) {
    String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
    String methodName = joinPoint.getSignature().getName();

    log.info("Method return: (class : {}, method : {})", className, methodName);
  }

}
