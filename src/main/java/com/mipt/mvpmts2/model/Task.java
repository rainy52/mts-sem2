package com.mipt.mvpmts2.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a task handled by the application.
 */
public class Task {
  private Long id;
  private String title;
  private String description;
  private boolean completed;
  private LocalDateTime createdAt;
  private LocalDate dueDate;
  private Priority priority;
  private Set<String> tags;

  public Task() {
    this.tags = new LinkedHashSet<>();
    this.priority = Priority.MEDIUM;
  }

  public Task(Long id, String title, String description, boolean completed) {
    this(id, title, description, completed, null, null, Priority.MEDIUM, Set.of());
  }

  public Task(
      Long id,
      String title,
      String description,
      boolean completed,
      LocalDateTime createdAt,
      LocalDate dueDate,
      Priority priority,
      Set<String> tags) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.completed = completed;
    this.createdAt = createdAt;
    this.dueDate = dueDate;
    this.priority = priority == null ? Priority.MEDIUM : priority;
    setTags(tags);
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
    this.priority = priority == null ? Priority.MEDIUM : priority;
  }

  public Set<String> getTags() {
    return new LinkedHashSet<>(tags);
  }

  public void setTags(Set<String> tags) {
    this.tags = tags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tags);
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
        && Objects.equals(description, task.description)
        && Objects.equals(createdAt, task.createdAt)
        && Objects.equals(dueDate, task.dueDate)
        && priority == task.priority
        && Objects.equals(tags, task.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, completed, createdAt, dueDate, priority, tags);
  }

  @Override
  public String toString() {
    return "Task{"
        + "id=" + id
        + ", title='" + title + '\''
        + ", description='" + description + '\''
        + ", completed=" + completed
        + ", createdAt=" + createdAt
        + ", dueDate=" + dueDate
        + ", priority=" + priority
        + ", tags=" + tags
        + '}';
  }
}
