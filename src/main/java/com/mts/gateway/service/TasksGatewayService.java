package com.mts.gateway.service;

import com.mts.gateway.client.ExternalTasksClient;
import com.mts.gateway.dto.TaskDto;
import com.mts.gateway.exception.TaskNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Service
public class TasksGatewayService {

    private static final Logger log = LoggerFactory.getLogger(TasksGatewayService.class);

    private final ExternalTasksClient client;

    public TasksGatewayService(ExternalTasksClient client) {
        this.client = client;
    }

    @RateLimiter(name = "externalApi", fallbackMethod = "createTaskFallback")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "createTaskFallback")
    public URI createTask(TaskDto taskDto) {
        return client.createTask(taskDto);
    }

    public URI createTaskFallback(TaskDto taskDto, Throwable t) {
        log.warn("Fallback for createTask. Error: {}", t.getMessage());
        return URI.create("/api/v1/tasks/fallback-id");
    }

    @RateLimiter(name = "externalApi", fallbackMethod = "getTaskFallback")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "getTaskFallback")
    public TaskDto getTask(String id) {
        return client.getTask(id);
    }

    public TaskDto getTaskFallback(String id, Throwable t) {
        if (t instanceof TaskNotFoundException) {
            throw (TaskNotFoundException) t;
        }
        log.warn("Fallback for getTask. Error: {}", t.getMessage());
        return new TaskDto("fallback-" + id, "Fallback Task", false);
    }

    @RateLimiter(name = "externalApi", fallbackMethod = "getTasksFallback")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "getTasksFallback")
    public List<TaskDto> getTasks(Boolean completed, Integer limit) {
        return client.getTasks(completed, limit);
    }

    public List<TaskDto> getTasksFallback(Boolean completed, Integer limit, Throwable t) {
        log.warn("Fallback for getTasks. Error: {}", t.getMessage());
        return Collections.emptyList();
    }

    @RateLimiter(name = "externalApi", fallbackMethod = "deleteTaskFallback")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "deleteTaskFallback")
    public void deleteTask(String id) {
        client.deleteTask(id);
    }

    public void deleteTaskFallback(String id, Throwable t) {
        log.warn("Fallback for deleteTask. Error: {}", t.getMessage());
    }
}
