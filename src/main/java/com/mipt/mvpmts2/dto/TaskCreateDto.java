package com.mipt.mvpmts2.dto;

import com.mipt.mvpmts2.model.Priority;
import com.mipt.mvpmts2.validation.OnCreate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Schema(description = "Payload for creating a task.")
public class TaskCreateDto {

  @Schema(description = "Task title.", example = "Prepare sprint demo")
  @NotBlank(message = "Task title must not be blank.", groups = OnCreate.class)
  @Size(min = 3, max = 100, message = "Task title must be between 3 and 100 characters.", groups = OnCreate.class)
  private String title;

  @Schema(description = "Task description.", example = "Collect status updates and polish slides.")
  @Size(max = 500, message = "Task description must not exceed 500 characters.", groups = OnCreate.class)
  private String description;

  @Schema(description = "Date by which the task should be completed.", example = "2026-03-30")
  @FutureOrPresent(message = "Due date must be today or in the future.", groups = OnCreate.class)
  private LocalDate dueDate;

  @Schema(description = "Task priority.", example = "HIGH")
  @NotNull(message = "Task priority must be provided.", groups = OnCreate.class)
  private Priority priority;

  @Schema(description = "Task tags.", example = "[\"backend\", \"urgent\"]")
  @Size(max = 5, message = "A task can have at most 5 tags.", groups = OnCreate.class)
  private Set<String> tags = new LinkedHashSet<>();

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  public Set<String> getTags() {
    return tags;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tags);
  }
}
