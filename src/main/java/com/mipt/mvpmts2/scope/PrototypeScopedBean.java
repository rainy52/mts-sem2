package com.mipt.mvpmts2.scope;

import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PrototypeScopedBean {

  private static final Logger logger = LoggerFactory.getLogger(PrototypeScopedBean.class);

  private final String instanceId;
  private final LocalDateTime createdAt;

  public PrototypeScopedBean() {
    this.instanceId = UUID.randomUUID().toString();
    this.createdAt = LocalDateTime.now();
    logger.info("PrototypeScopedBean created: {}", instanceId);
  }

  public Long generateTaskId() {
    UUID uuid = UUID.randomUUID();
    long taskId = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
    if (taskId == Long.MIN_VALUE) {
      taskId = 0L;
    }
    taskId = Math.abs(taskId);
    logger.info("Generated task id {} using prototype instance {}", taskId, instanceId);
    return taskId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public String toString() {
    return "PrototypeScopedBean{"
        + "instanceId='" + instanceId + '\''
        + ", createdAt=" + createdAt
        + '}';
  }
}
