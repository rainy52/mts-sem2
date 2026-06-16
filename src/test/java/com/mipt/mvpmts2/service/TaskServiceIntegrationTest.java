package com.mipt.mvpmts2.service;

import com.mipt.mvpmts2.model.Priority;
import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testBulkCompleteTasksRollback() {
        Task task1 = new Task(null, "Task A", "Desc A", false, LocalDateTime.now(), LocalDate.now().plusDays(1), Priority.MEDIUM, Set.of());
        Task task2 = new Task(null, "Task B", "Desc B", false, LocalDateTime.now(), LocalDate.now().plusDays(1), Priority.MEDIUM, Set.of());
        
        task1 = taskRepository.save(task1);
        task2 = taskRepository.save(task2);

        Long validId1 = task1.getId();
        Long validId2 = task2.getId();
        Long invalidId = -999L;

        assertThrows(TaskNotFoundException.class, () -> taskService.bulkCompleteTasks(List.of(validId1, validId2, invalidId)));

        Task foundTask1 = taskRepository.findById(validId1).orElseThrow();
        Task foundTask2 = taskRepository.findById(validId2).orElseThrow();

        assertThat(foundTask1.isCompleted()).isFalse();
        assertThat(foundTask2.isCompleted()).isFalse();
    }
}
