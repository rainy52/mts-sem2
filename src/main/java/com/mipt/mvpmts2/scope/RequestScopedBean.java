package com.mipt.mvpmts2.scope;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class RequestScopedBean {

  private static final Logger logger = LoggerFactory.getLogger(RequestScopedBean.class);

  private final String requestId;
  private final LocalDateTime requestStartTime;

  public RequestScopedBean() {
    this.requestId = UUID.randomUUID().toString();
    this.requestStartTime = LocalDateTime.now();
    logger.info("RequestScopedBean created for request {}", requestId);
  }

  public String getRequestId() {
    return requestId;
  }

  public LocalDateTime getRequestStartTime() {
    return requestStartTime;
  }

  public long getRequestDurationMillis() {
    return ChronoUnit.MILLIS.between(requestStartTime, LocalDateTime.now());
  }

  @Override
  public String toString() {
    return "RequestScopedBean{"
        + "requestId='" + requestId + '\''
        + ", requestStartTime=" + requestStartTime
        + ", durationMillis=" + getRequestDurationMillis()
        + '}';
  }
}
