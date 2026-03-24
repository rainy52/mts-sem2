package com.mipt.mvpmts2.aspect;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logs method calls.
 */
@Aspect
@Component
public class LoggingAspect {

  private static final Logger logger = LoggerFactory.getLogger("service");

  @Around("within(com.mipt.mvpmts2.service..*) && execution(public * *(..))")
  public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    String className = joinPoint.getSignature().getDeclaringTypeName();
    String methodName = joinPoint.getSignature().getName();
    Object[] arguments = joinPoint.getArgs();

    logger.info("START {}.{}({})", className, methodName, Arrays.toString(arguments));

    try {
      Object result = joinPoint.proceed();
      logger.info("END {}.{} -> {}", className, methodName, result == null ? "void" : result);
      return result;
    } catch (Throwable exception) {
      logger.error("ERROR {}.{} -> {}", className, methodName, exception.getMessage(), exception);
      throw exception;
    }
  }
}
