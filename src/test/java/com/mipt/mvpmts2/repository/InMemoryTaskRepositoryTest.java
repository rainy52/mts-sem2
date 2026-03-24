package com.mipt.mvpmts2.repository;

import com.mipt.mvpmts2.model.Task;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTaskRepositoryTest {

  private InMemoryTaskRepository repository;

  @BeforeEach
  void setUp() {
    repository = new InMemoryTaskRepository();
  }

  @Test
  void savePositiveCreate() {
    Task saved = repository.save(new Task(null, "New task", "Description", false));

    assertNotNull(saved);
    assertNotNull(saved.getId());
    assertEquals("New task", saved.getTitle());
  }

  @Test
  void savePositiveUpdate() {
    Task saved = repository.save(new Task(null, "Task", "Description", false));
    saved.setTitle("Updated task");

    Task updated = repository.save(saved);

    assertEquals(saved.getId(), updated.getId());
    assertEquals("Updated task", updated.getTitle());
  }

  @Test
  void findByIdPositive() {
    Task saved = repository.save(new Task(null, "Task", "Description", false));

    Optional<Task> found = repository.findById(saved.getId());

    assertTrue(found.isPresent());
    assertEquals(saved.getId(), found.get().getId());
  }

  @Test
  void findByIdNegativeNotFound() {
    Optional<Task> found = repository.findById(999L);

    assertFalse(found.isPresent());
  }

  @Test
  void findAllPositive() {
    repository.save(new Task(null, "Task 1", "Description 1", false));
    repository.save(new Task(null, "Task 2", "Description 2", true));

    List<Task> tasks = repository.findAll();

    assertNotNull(tasks);
    assertEquals(2, tasks.size());
  }

  @Test
  void deleteByIdPositive() {
    Task saved = repository.save(new Task(null, "Task", "Description", false));

    repository.deleteById(saved.getId());

    assertFalse(repository.findById(saved.getId()).isPresent());
  }

  @Test
  void updatePositive() {
    Task saved = repository.save(new Task(null, "Task", "Description", false));
    saved.setTitle("Updated");
    saved.setCompleted(true);

    Task updated = repository.update(saved);

    assertEquals("Updated", updated.getTitle());
    assertTrue(updated.isCompleted());
  }

  @Test
  void updateNegativeNotFound() {
    Task task = new Task(999L, "Task", "Description", false);

    assertThrows(NoSuchElementException.class, () -> repository.update(task));
  }

  @Test
  void existsByIdPositive() {
    Task saved = repository.save(new Task(null, "Task", "Description", false));

    assertTrue(repository.existsById(saved.getId()));
  }
}
