package com.mipt.mvpmts2.dto;

import com.mipt.mvpmts2.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Task data returned to API consumers.")
public class TaskResponseDto {
  @Schema(description = "Task identifier.", example = "42")
  private Long id;

  @Schema(description = "Task title.", example = "Prepare sprint demo")
  private String title;

  @Schema(description = "Task description.", example = "Collect updates and polish slides.")
  private String description;

  @Schema(description = "Task completion flag.", example = "false")
  private boolean completed;

  @Schema(description = "Task creation timestamp.", example = "2026-03-24T20:15:00")
  private LocalDateTime createdAt;

  @Schema(description = "Task due date.", example = "2026-03-30")
  private LocalDate dueDate;

  @Schema(description = "Task priority.", example = "HIGH")
  private Priority priority;

  @Schema(description = "Task tags.", example = "[\"backend\", \"urgent\"]")
  private Set<String> tags;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
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
