package com.mipt.mvpmts2.service;

import com.mipt.mvpmts2.model.Priority;
import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private jakarta.servlet.http.HttpServletRequest httpServletRequest;

    @Test
    void givenExistingTask_whenUpdateTask_thenTaskIsUpdatedAndSaved() {
        Long taskId = 1L;
        Task existingTask = new Task(
                taskId,
                "Old Title",
                "Old Description",
                false,
                LocalDateTime.now().minusDays(1),
                LocalDate.now().plusDays(2),
                Priority.LOW,
                Set.of("old")
        );

        Task updateRequest = new Task(
                null,
                "New Title",
                "New Description",
                true,
                null,
                LocalDate.now().plusDays(5),
                Priority.HIGH,
                Set.of("new")
        );

        Task savedTask = new Task(
                taskId,
                "New Title",
                "New Description",
                true,
                existingTask.getCreatedAt(),
                LocalDate.now().plusDays(5),
                Priority.HIGH,
                Set.of("new")
        );

        given(taskRepository.findById(taskId)).willReturn(Optional.of(existingTask));
        given(taskRepository.save(any(Task.class))).willReturn(savedTask);

        Task result = taskService.updateTask(taskId, updateRequest);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task capturedTask = taskCaptor.getValue();
        assertEquals(taskId, capturedTask.getId());
        assertEquals("New Title", capturedTask.getTitle());
        assertEquals("New Description", capturedTask.getDescription());
        assertEquals(true, capturedTask.isCompleted());
        assertEquals(Priority.HIGH, capturedTask.getPriority());

        assertEquals(savedTask, result);
    }
}
