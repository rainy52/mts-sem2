package com.mipt.mvpmts2.controller;

import com.mipt.mvpmts2.dto.ErrorResponse;
import com.mipt.mvpmts2.dto.TaskCreateDto;
import com.mipt.mvpmts2.dto.TaskResponseDto;
import com.mipt.mvpmts2.dto.TaskUpdateDto;
import com.mipt.mvpmts2.mapper.TaskMapper;
import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.service.TaskService;
import com.mipt.mvpmts2.validation.OnCreate;
import com.mipt.mvpmts2.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@Validated
@Tag(name = "Tasks", description = "Task CRUD operations.")
public class TaskController {

  private final TaskService taskService;
  private final TaskMapper taskMapper;

  public TaskController(TaskService taskService, TaskMapper taskMapper) {
    this.taskService = taskService;
    this.taskMapper = taskMapper;
  }

  @Operation(summary = "Get all tasks")
  @ApiResponse(responseCode = "200", description = "Tasks returned successfully",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResponseDto.class))))
  @GetMapping
  public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
    List<TaskResponseDto> body = taskService.getAllTasks().stream()
        .map(taskMapper::toResponseDto)
        .toList();
    return ResponseEntity.ok()
        .header("X-Total-Count", String.valueOf(body.size()))
        .body(body);
  }

  @Operation(summary = "Get a task by id")
  @ApiResponse(responseCode = "200", description = "Task found",
      content = @Content(schema = @Schema(implementation = TaskResponseDto.class)))
  @ApiResponse(responseCode = "404", description = "Task not found",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @GetMapping("/{id}")
  public ResponseEntity<TaskResponseDto> getTask(@PathVariable Long id) {
    return ResponseEntity.ok(taskMapper.toResponseDto(taskService.getTaskOrThrow(id)));
  }

  @Operation(summary = "Create a task")
  @ApiResponse(responseCode = "201", description = "Task created",
      content = @Content(schema = @Schema(implementation = TaskResponseDto.class)))
  @ApiResponse(responseCode = "400", description = "Validation failed",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping
  public ResponseEntity<TaskResponseDto> createTask(@Validated(OnCreate.class) @RequestBody TaskCreateDto taskDto) {
    Task savedTask = taskService.createTask(taskMapper.toEntity(taskDto));
    return ResponseEntity.created(URI.create("/api/tasks/" + savedTask.getId()))
        .body(taskMapper.toResponseDto(savedTask));
  }

  @Operation(summary = "Update a task")
  @ApiResponse(responseCode = "200", description = "Task updated",
      content = @Content(schema = @Schema(implementation = TaskResponseDto.class)))
  @ApiResponse(responseCode = "400", description = "Validation failed",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Task not found",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PutMapping("/{id}")
  public ResponseEntity<TaskResponseDto> updateTask(
      @PathVariable Long id,
      @Validated(OnUpdate.class) @RequestBody TaskUpdateDto taskDto) {
    Task task = taskService.getTaskOrThrow(id);
    Task updatedTask = taskService.updateTask(id, taskMapper.updateEntity(taskDto, task));
    return ResponseEntity.ok(taskMapper.toResponseDto(updatedTask));
  }

  @Operation(summary = "Delete a task")
  @ApiResponse(responseCode = "204", description = "Task deleted")
  @ApiResponse(responseCode = "404", description = "Task not found",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
    taskService.deleteTask(id);
    return ResponseEntity.noContent().build();
  }
}
