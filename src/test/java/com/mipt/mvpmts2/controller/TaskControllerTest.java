package com.mipt.mvpmts2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.mvpmts2.dto.TaskCreateDto;
import com.mipt.mvpmts2.dto.TaskUpdateDto;
import com.mipt.mvpmts2.model.Priority;
import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.repository.InMemoryTaskRepository;
import com.mipt.mvpmts2.service.TaskService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private InMemoryTaskRepository taskRepository;

  @Autowired
  private TaskService taskService;

  @BeforeEach
  void setUp() {
    taskRepository.clear();
    taskService.clearCacheForTesting();
  }

  @Test
  void createTaskReturnsDtoAndHeaders() throws Exception {
    TaskCreateDto request = new TaskCreateDto();
    request.setTitle("Create task");
    request.setDescription("Create description");
    request.setDueDate(LocalDate.now().plusDays(1));
    request.setPriority(Priority.HIGH);
    request.setTags(Set.of("api", "backend"));

    mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.title").value("Create task"))
        .andExpect(jsonPath("$.priority").value("HIGH"))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.tags", hasSize(2)));
  }

  @Test
  void createTaskRejectsInvalidPayload() throws Exception {
    TaskCreateDto request = new TaskCreateDto();
    request.setTitle(" ");
    request.setDescription("x".repeat(20));
    request.setDueDate(LocalDate.now().minusDays(1));

    mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.details.fieldErrors.title").isArray())
        .andExpect(jsonPath("$.details.fieldErrors.dueDate").isArray())
        .andExpect(jsonPath("$.details.fieldErrors.priority").isArray());
  }

  @Test
  void getAllTasksAddsTotalCountHeader() throws Exception {
    taskRepository.save(new Task(
        null,
        "First task",
        "First description",
        false,
        LocalDateTime.now(),
        LocalDate.now().plusDays(2),
        Priority.MEDIUM,
        Set.of("first")
    ));
    taskRepository.save(new Task(
        null,
        "Second task",
        "Second description",
        true,
        LocalDateTime.now(),
        LocalDate.now().plusDays(3),
        Priority.LOW,
        Set.of("second")
    ));

    mockMvc.perform(get("/api/tasks"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Total-Count", "2"))
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void updateTaskPartiallyUpdatesExistingTask() throws Exception {
    Task saved = taskRepository.save(new Task(
        null,
        "Original task",
        "Original description",
        false,
        LocalDateTime.now(),
        LocalDate.now().plusDays(5),
        Priority.MEDIUM,
        Set.of("original")
    ));

    TaskUpdateDto request = new TaskUpdateDto();
    request.setTitle("Updated task");
    request.setCompleted(true);
    request.setPriority(Priority.HIGH);

    mockMvc.perform(put("/api/tasks/{id}", saved.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
        .andExpect(jsonPath("$.title").value("Updated task"))
        .andExpect(jsonPath("$.completed").value(true))
        .andExpect(jsonPath("$.description").value("Original description"))
        .andExpect(jsonPath("$.priority").value("HIGH"));
  }

  @Test
  void updateTaskRejectsDueDateBeforeCreationDate() throws Exception {
    Task saved = taskRepository.save(new Task(
        null,
        "Future-created task",
        "Description",
        false,
        LocalDateTime.now().plusDays(5),
        LocalDate.now().plusDays(10),
        Priority.MEDIUM,
        Set.of("future")
    ));

    TaskUpdateDto request = new TaskUpdateDto();
    request.setDueDate(LocalDate.now().plusDays(1));

    mockMvc.perform(put("/api/tasks/{id}", saved.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed."));
  }

  @Test
  void getMissingTaskReturnsUnified404Response() throws Exception {
    mockMvc.perform(get("/api/tasks/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Task with id 999 was not found."))
        .andExpect(jsonPath("$.path").value("/api/tasks/999"));
  }
}
