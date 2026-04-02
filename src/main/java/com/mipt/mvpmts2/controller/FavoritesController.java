package com.mipt.mvpmts2.controller;

import com.mipt.mvpmts2.dto.ErrorResponse;
import com.mipt.mvpmts2.dto.TaskResponseDto;
import com.mipt.mvpmts2.mapper.TaskMapper;
import com.mipt.mvpmts2.service.FavoritesService;
import com.mipt.mvpmts2.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favorites", description = "Session-based favorite tasks.")
public class FavoritesController {

  private final FavoritesService favoritesService;
  private final TaskService taskService;
  private final TaskMapper taskMapper;

  public FavoritesController(FavoritesService favoritesService, TaskService taskService, TaskMapper taskMapper) {
    this.favoritesService = favoritesService;
    this.taskService = taskService;
    this.taskMapper = taskMapper;
  }

  @Operation(summary = "Add a task to favorites")
  @ApiResponse(responseCode = "200", description = "Task added to favorites")
  @ApiResponse(responseCode = "404", description = "Task not found",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/{taskId}")
  public ResponseEntity<Void> addFavorite(@PathVariable Long taskId, HttpSession session) {
    taskService.getTaskOrThrow(taskId);
    favoritesService.addFavorite(session, taskId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Remove a task from favorites")
  @ApiResponse(responseCode = "204", description = "Task removed from favorites")
  @DeleteMapping("/{taskId}")
  public ResponseEntity<Void> removeFavorite(@PathVariable Long taskId, HttpSession session) {
    favoritesService.removeFavorite(session, taskId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Get favorite tasks")
  @ApiResponse(responseCode = "200", description = "Favorite tasks returned",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResponseDto.class))))
  @GetMapping
  public ResponseEntity<List<TaskResponseDto>> getFavorites(HttpSession session) {
    List<TaskResponseDto> body = taskService.getTasksByIds(favoritesService.getFavoriteTaskIds(session)).stream()
        .map(taskMapper::toResponseDto)
        .toList();
    return ResponseEntity.ok(body);
  }
}
