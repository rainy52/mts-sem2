package com.mipt.mvpmts2.mapper;

import com.mipt.mvpmts2.dto.TaskCreateDto;
import com.mipt.mvpmts2.dto.TaskUpdateDto;
import com.mipt.mvpmts2.model.Priority;
import com.mipt.mvpmts2.model.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskMapperTest {

  private final TaskMapper mapper = new TaskMapperImpl();

  @Test
  void toEntityMapsCreateDtoToTask() {
    TaskCreateDto dto = new TaskCreateDto();
    dto.setTitle("Create");
    dto.setDescription("Description");
    dto.setDueDate(LocalDate.now().plusDays(1));
    dto.setPriority(Priority.HIGH);
    dto.setTags(Set.of("api"));

    Task task = mapper.toEntity(dto);

    assertNull(task.getId());
    assertNull(task.getCreatedAt());
    assertEquals("Create", task.getTitle());
    assertEquals(Priority.HIGH, task.getPriority());
    assertFalse(task.isCompleted());
    assertEquals(Set.of("api"), task.getTags());
  }

  @Test
  void updateEntityIgnoresNullFields() {
    Task task = new Task(
        1L,
        "Original",
        "Description",
        false,
        LocalDateTime.now(),
        LocalDate.now().plusDays(2),
        Priority.MEDIUM,
        Set.of("keep")
    );
    TaskUpdateDto dto = new TaskUpdateDto();
    dto.setCompleted(true);

    mapper.updateEntity(dto, task);

    assertEquals("Original", task.getTitle());
    assertTrue(task.isCompleted());
    assertEquals(Set.of("keep"), task.getTags());
  }
}
