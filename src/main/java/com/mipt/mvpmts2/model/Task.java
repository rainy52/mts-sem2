package com.mipt.mvpmts2.model;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Represents a task handled by the application.
 */
public class Task {
  private Long id;

  @NotBlank(message = "Task title must not be blank.")
  private String title;

  private String description;
  private boolean completed;

  public Task() {
  }

  public Task(Long id, String title, String description, boolean completed) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.completed = completed;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Task task = (Task) o;
    return completed == task.completed
        && Objects.equals(id, task.id)
        && Objects.equals(title, task.title)
        && Objects.equals(description, task.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, completed);
  }

  @Override
  public String toString() {
    return "Task{"
        + "id=" + id
        + ", title='" + title + '\''
        + ", description='" + description + '\''
        + ", completed=" + completed
        + '}';
  }
}
