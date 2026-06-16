package com.mipt.mvpmts2.controller;

import com.mipt.mvpmts2.service.TaskService;
import com.mipt.mvpmts2.service.TaskStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diagnostics")
@Tag(name = "Diagnostics", description = "Auxiliary endpoints for demo and diagnostics.")
public class TaskDiagnosticsController {

  private final TaskService taskService;
  private final TaskStatisticsService taskStatisticsService;

  public TaskDiagnosticsController(
      TaskService taskService,
      TaskStatisticsService taskStatisticsService) {
    this.taskService = taskService;
    this.taskStatisticsService = taskStatisticsService;
  }

  @GetMapping("/repositories")
  @Operation(summary = "Compare configured repositories")
  public ResponseEntity<TaskStatisticsService.StatisticsResult> getRepositoryStatistics() {
    return ResponseEntity.ok(taskStatisticsService.compareRepositories());
  }

  @GetMapping("/app-info")
  @Operation(summary = "Get application metadata")
  public ResponseEntity<TaskService.AppInfo> getAppInfo() {
    return ResponseEntity.ok(taskService.getAppInfo());
  }

  @GetMapping("/cache-statistics")
  @Operation(summary = "Get task cache statistics")
  public ResponseEntity<TaskService.CacheStatistics> getCacheStatistics() {
    return ResponseEntity.ok(taskService.getCacheStatistics());
  }

  @GetMapping("/request-info")
  @Operation(summary = "Get request-scope diagnostic information")
  public ResponseEntity<TaskService.RequestInfo> getRequestInfo() {
    return ResponseEntity.ok(taskService.getRequestInfo());
  }

  @GetMapping("/prototype-info")
  @Operation(summary = "Get prototype-scope diagnostic information")
  public ResponseEntity<TaskService.PrototypeInfo> getPrototypeInfo() {
    return ResponseEntity.ok(taskService.getPrototypeInfo());
  }
}
