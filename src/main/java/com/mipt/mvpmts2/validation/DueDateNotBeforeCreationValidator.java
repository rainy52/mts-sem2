package com.mipt.mvpmts2.validation;

import com.mipt.mvpmts2.dto.TaskUpdateDto;
import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class DueDateNotBeforeCreationValidator implements ConstraintValidator<DueDateNotBeforeCreation, TaskUpdateDto> {

  private final TaskService taskService;
  private final HttpServletRequest request;

  public DueDateNotBeforeCreationValidator(TaskService taskService, HttpServletRequest request) {
    this.taskService = taskService;
    this.request = request;
  }

  @Override
  public boolean isValid(TaskUpdateDto value, ConstraintValidatorContext context) {
    if (value == null || value.getDueDate() == null) {
      return true;
    }

    Long taskId = resolveTaskId();
    if (taskId == null) {
      return true;
    }

    return taskService.getTask(taskId)
        .map(Task::getCreatedAt)
        .filter(createdAt -> createdAt != null)
        .map(LocalDateTime::toLocalDate)
        .map(createdDate -> !value.getDueDate().isBefore(createdDate))
        .orElse(true);
  }

  private Long resolveTaskId() {
    Object rawVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    if (!(rawVariables instanceof Map<?, ?> variables)) {
      return null;
    }

    Object rawId = variables.get("id");
    if (rawId == null) {
      return null;
    }

    try {
      return Long.valueOf(rawId.toString());
    } catch (NumberFormatException exception) {
      return null;
    }
  }
}
