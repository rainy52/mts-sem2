package com.mipt.mvpmts2.repository;

import com.mipt.mvpmts2.model.Task;
import java.util.List;
import java.util.Optional;

/**
 * Provides a fixed task set that is useful for demonstrating bean qualification.
 */
public class StubTaskRepository implements TaskRepository {

  private final List<Task> stubTasks = List.of(
      new Task(1L, "Stub task 1", "Stub description 1", false),
      new Task(2L, "Stub task 2", "Stub description 2", true),
      new Task(3L, "Stub task 3", "Stub description 3", false)
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
    return new Task(task.getId(), task.getTitle(), task.getDescription(), task.isCompleted());
  }
}
