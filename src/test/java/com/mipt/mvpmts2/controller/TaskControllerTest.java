package com.mipt.mvpmts2.controller;

import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.repository.TaskRepository;
import com.mipt.mvpmts2.service.TaskService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskControllerTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private TaskService taskService;

  @MockBean(name = "inMemoryTaskRepository")
  private TaskRepository taskRepository;

  @BeforeEach
  void setUp() {
    reset(taskRepository);
    taskService.clearCacheForTesting();
  }

  @Test
  void createTaskPositive() {
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    ResponseEntity<Task> response = restTemplate.postForEntity(
        baseUrl(),
        new HttpEntity<>(new Task(null, "Create task", "Create description", false), jsonHeaders()),
        Task.class
    );

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getId());
    assertEquals("Create task", response.getBody().getTitle());
  }

  @Test
  void createTaskNegative() {
    ResponseEntity<ApiErrorResponse> response = restTemplate.postForEntity(
        baseUrl(),
        new HttpEntity<>(new Task(null, " ", "Invalid description", false), jsonHeaders()),
        ApiErrorResponse.class
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().message().contains("Task title"));
  }

  @Test
  void getAllTasksPositive() {
    when(taskRepository.findAll()).thenReturn(List.of(
        new Task(10L, "First task", "First description", false),
        new Task(20L, "Second task", "Second description", true)
    ));

    ResponseEntity<Task[]> response = restTemplate.getForEntity(baseUrl(), Task[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().length);
  }

  @Test
  void getAllTasksNegative() {
    when(taskRepository.findAll()).thenThrow(new IllegalStateException("Repository is unavailable."));

    ResponseEntity<ApiErrorResponse> response = restTemplate.getForEntity(baseUrl(), ApiErrorResponse.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().message().contains("Repository is unavailable"));
  }

  @Test
  void getTaskByIdPositive() {
    when(taskRepository.findById(11L)).thenReturn(Optional.of(new Task(11L, "Find task", "Find description", false)));

    ResponseEntity<Task> response = restTemplate.getForEntity(baseUrl() + "/11", Task.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(11L, response.getBody().getId());
  }

  @Test
  void getTaskByIdNegative() {
    when(taskRepository.findById(404L)).thenReturn(Optional.empty());

    ResponseEntity<Task> response = restTemplate.getForEntity(baseUrl() + "/404", Task.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void updateTaskPositive() {
    when(taskRepository.existsById(33L)).thenReturn(true);
    when(taskRepository.update(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    ResponseEntity<Task> response = restTemplate.exchange(
        baseUrl() + "/33",
        HttpMethod.PUT,
        new HttpEntity<>(new Task(null, "Updated task", "Updated description", true), jsonHeaders()),
        Task.class
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(33L, response.getBody().getId());
    assertTrue(response.getBody().isCompleted());
  }

  @Test
  void updateTaskNegative() {
    when(taskRepository.existsById(303L)).thenReturn(false);

    ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(
        baseUrl() + "/303",
        HttpMethod.PUT,
        new HttpEntity<>(new Task(null, "Missing task", "Missing description", true), jsonHeaders()),
        ApiErrorResponse.class
    );

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().message().contains("303"));
  }

  @Test
  void deleteTaskPositive() {
    when(taskRepository.existsById(77L)).thenReturn(true);
    doNothing().when(taskRepository).deleteById(77L);

    ResponseEntity<Void> response = restTemplate.exchange(baseUrl() + "/77", HttpMethod.DELETE, null, Void.class);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void deleteTaskNegative() {
    when(taskRepository.existsById(88L)).thenReturn(false);

    ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(
        baseUrl() + "/88",
        HttpMethod.DELETE,
        null,
        ApiErrorResponse.class
    );

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().message().contains("88"));
  }

  private String baseUrl() {
    return "http://localhost:" + port + "/api/tasks";
  }

  private HttpHeaders jsonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
