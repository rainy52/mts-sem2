package com.mipt.mvpmts2.repository;

import com.mipt.mvpmts2.model.Priority;
import com.mipt.mvpmts2.model.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides a fixed task set that is useful for demonstrating bean qualification.
 */
public class StubTaskRepository implements TaskRepository {

  private final List<Task> stubTasks = List.of(
      new Task(1L, "Stub task 1", "Stub description 1", false,
          LocalDateTime.now().minusDays(3), LocalDate.now().plusDays(2), Priority.LOW, Set.of("stub")),
      new Task(2L, "Stub task 2", "Stub description 2", true,
          LocalDateTime.now().minusDays(2), LocalDate.now().plusDays(4), Priority.MEDIUM, Set.of("demo")),
      new Task(3L, "Stub task 3", "Stub description 3", false,
          LocalDateTime.now().minusDays(1), LocalDate.now().plusDays(6), Priority.HIGH, Set.of("reference"))
  );

  @Override
  public Task save(Task task) {
    return copy(task);
  }

  @Override
  public Optional<Task> findById(Long id) {
    return stubTasks.stream()
        .filter(task -> task.getId().equals(id))
        .map(this::copy)
        .findFirst();
  }

  @Override
  public List<Task> findAll() {
    return stubTasks.stream().map(this::copy).toList();
  }

  @Override
  public void deleteById(Long id) {
  }

  @Override
  public Task update(Task task) {
    return copy(task);
  }

  @Override
  public boolean existsById(Long id) {
    return stubTasks.stream().anyMatch(task -> task.getId().equals(id));
  }

  private Task copy(Task task) {
    return new Task(
        task.getId(),
        task.getTitle(),
        task.getDescription(),
        task.isCompleted(),
        task.getCreatedAt(),
        task.getDueDate(),
        task.getPriority(),
        task.getTags()
    );
  }
}
