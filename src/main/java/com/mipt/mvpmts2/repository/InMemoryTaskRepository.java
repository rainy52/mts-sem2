package com.mipt.mvpmts2.repository;

import com.mipt.mvpmts2.model.Task;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * Stores tasks in memory. It is the main repository implementation.
 */
@Repository
@Primary
public class InMemoryTaskRepository implements TaskRepository {

  private final Map<Long, Task> storage = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public Task save(Task task) {
    Task taskToStore = copy(task);
    if (taskToStore.getId() == null) {
      taskToStore.setId(idGenerator.getAndIncrement());
    } else {
      idGenerator.updateAndGet(current -> Math.max(current, taskToStore.getId() + 1));
    }
    storage.put(taskToStore.getId(), taskToStore);
    return copy(taskToStore);
  }

  @Override
  public Optional<Task> findById(Long id) {
    return Optional.ofNullable(storage.get(id)).map(this::copy);
  }

  @Override
  public List<Task> findAll() {
    return storage.values().stream()
        .map(this::copy)
        .sorted(Comparator.comparing(Task::getId))
        .toList();
  }

  @Override
  public void deleteById(Long id) {
    storage.remove(id);
  }

  @Override
  public Task update(Task task) {
    if (!existsById(task.getId())) {
      throw new NoSuchElementException("Task with id " + task.getId() + " was not found.");
    }
    Task taskToStore = copy(task);
    storage.put(taskToStore.getId(), taskToStore);
    return copy(taskToStore);
  }

  @Override
  public boolean existsById(Long id) {
    return storage.containsKey(id);
  }

  public void clear() {
    storage.clear();
    idGenerator.set(1);
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
