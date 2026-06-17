package com.mts.gateway.external;

import com.mts.gateway.dto.TaskDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/external/v1")
public class ExternalApiController {

    private final Map<String, TaskDto> tasks = new ConcurrentHashMap<>();

    @PostMapping("/tasks")
    public ResponseEntity<Void> createTask(@RequestBody TaskDto task) {
        String id = UUID.randomUUID().toString();
        task.setId(id);
        tasks.put(id, task);
        return ResponseEntity.created(URI.create("/external/v1/tasks/" + id)).build();
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<?> getTask(@PathVariable String id) {
        TaskDto task = tasks.get(id);
        if (task == null) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Task with ID " + id + " not found in external system");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDto>> getTasks(@RequestParam(required = false) Boolean completed,
                                                  @RequestParam(required = false) Integer limit) {
        List<TaskDto> result = new ArrayList<>(tasks.values());
        if (completed != null) {
            result = result.stream().filter(t -> t.isCompleted() == completed).collect(Collectors.toList());
        }
        if (limit != null && limit > 0 && limit < result.size()) {
            result = result.subList(0, limit);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        tasks.remove(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/unstable")
    public ResponseEntity<?> unstableEndpoint(@RequestParam(required = false) String mode) throws InterruptedException {
        if ("timeout".equals(mode)) {
            Thread.sleep(5000);
            return ResponseEntity.ok(new TaskDto("timeout", "timeout task", false));
        } else if ("500".equals(mode)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error");
        } else if ("429".equals(mode)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", "10")
                    .body("Too many requests");
        } else if ("html".equals(mode)) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.TEXT_HTML)
                    .body("<html><body><h1>Bad Gateway</h1></body></html>");
        }
        return ResponseEntity.ok("Stable");
    }
}
