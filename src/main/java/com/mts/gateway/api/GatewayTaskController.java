package com.mts.gateway.api;

import com.mts.gateway.dto.TaskDto;
import com.mts.gateway.service.TasksGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class GatewayTaskController {

    private final TasksGatewayService service;

    public GatewayTaskController(TasksGatewayService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> createTask(@RequestBody TaskDto taskDto) {
        URI location = service.createTask(taskDto);
        if (location != null && location.getPath() != null) {
            return ResponseEntity.created(location).build();
        }
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable String id) {
        return ResponseEntity.ok(service.getTask(id));
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(@RequestParam(required = false) Boolean completed,
                                                  @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(service.getTasks(completed, limit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        service.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
