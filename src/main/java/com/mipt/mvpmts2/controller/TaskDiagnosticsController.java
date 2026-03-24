package com.mipt.mvpmts2.controller;

import com.mipt.mvpmts2.service.TaskService;
import com.mipt.mvpmts2.service.TaskStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes auxiliary endpoints used to demonstrate bean scopes, configuration and repository wiring.
 */
@RestController
@RequestMapping("/api/diagnostics")
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
  public ResponseEntity<TaskStatisticsService.StatisticsResult> getRepositoryStatistics() {
    return ResponseEntity.ok(taskStatisticsService.compareRepositories());
  }

  @GetMapping("/app-info")
  public ResponseEntity<TaskService.AppInfo> getAppInfo() {
    return ResponseEntity.ok(taskService.getAppInfo());
  }

  @GetMapping("/cache-statistics")
  public ResponseEntity<TaskService.CacheStatistics> getCacheStatistics() {
    return ResponseEntity.ok(taskService.getCacheStatistics());
  }

  @GetMapping("/request-info")
  public ResponseEntity<TaskService.RequestInfo> getRequestInfo() {
    return ResponseEntity.ok(taskService.getRequestInfo());
  }

  @GetMapping("/prototype-info")
  public ResponseEntity<TaskService.PrototypeInfo> getPrototypeInfo() {
    return ResponseEntity.ok(taskService.getPrototypeInfo());
  }
}
