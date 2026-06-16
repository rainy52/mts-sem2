package com.mipt.mvpmts2.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "completed", nullable = false)
  private boolean completed;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "last_modified_date")
  private LocalDateTime lastModifiedDate;

  @Column(name = "due_date")
  private LocalDate dueDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority")
  private Priority priority;

  @Convert(converter = TagsConverter.class)
  @Column(name = "tags", columnDefinition = "TEXT")
  private Set<String> tags;

  @OneToMany(mappedBy = "task", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
  private List<TaskAttachment> attachments = new ArrayList<>();

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

  public LocalDateTime getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
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

  public List<TaskAttachment> getAttachments() {
    return attachments;
  }

  public void setAttachments(List<TaskAttachment> attachments) {
    this.attachments = attachments;
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
        + ", lastModifiedDate=" + lastModifiedDate
        + ", dueDate=" + dueDate
        + ", priority=" + priority
        + ", tags=" + tags
        + '}';
  }
}
