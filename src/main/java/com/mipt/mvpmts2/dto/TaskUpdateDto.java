package com.mipt.mvpmts2.dto;

import com.mipt.mvpmts2.model.Priority;
import com.mipt.mvpmts2.validation.DueDateNotBeforeCreation;
import com.mipt.mvpmts2.validation.OnUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@DueDateNotBeforeCreation(groups = OnUpdate.class)
@Schema(description = "Payload for partially updating a task.")
public class TaskUpdateDto {

  @Schema(description = "Updated title.", example = "Ship sprint demo")
  @Pattern(
      regexp = "^(?=.*\\S).*$",
      message = "Task title must not be blank when provided.",
      groups = OnUpdate.class)
  @Size(min = 3, max = 100, message = "Task title must be between 3 and 100 characters.", groups = OnUpdate.class)
  private String title;

  @Schema(description = "Updated description.", example = "Slides are ready; rehearse once more.")
  @Size(max = 500, message = "Task description must not exceed 500 characters.", groups = OnUpdate.class)
  private String description;

  @Schema(description = "Completion flag.", example = "true")
  private Boolean completed;

  @Schema(description = "Updated due date.", example = "2026-03-31")
  @FutureOrPresent(message = "Due date must be today or in the future.", groups = OnUpdate.class)
  private LocalDate dueDate;

  @Schema(description = "Updated priority.", example = "MEDIUM")
  private Priority priority;

  @Schema(description = "Updated tags.", example = "[\"backend\", \"demo\"]")
  @Size(max = 5, message = "A task can have at most 5 tags.", groups = OnUpdate.class)
  private Set<String> tags;

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

  public Boolean getCompleted() {
    return completed;
  }

  public void setCompleted(Boolean completed) {
    this.completed = completed;
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
    this.tags = tags;
  }
}
