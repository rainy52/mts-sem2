package com.mipt.mvpmts2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.mvpmts2.dto.TaskCreateDto;
import com.mipt.mvpmts2.dto.TaskResponseDto;
import com.mipt.mvpmts2.mapper.TaskMapper;
import com.mipt.mvpmts2.model.Priority;
import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskMapper taskMapper;

    @Test
    void testCreateTask() throws Exception {
        TaskCreateDto createDto = new TaskCreateDto();
        createDto.setTitle("New Task");
        createDto.setDescription("Description");
        createDto.setDueDate(LocalDate.now().plusDays(1));
        createDto.setPriority(Priority.HIGH);
        createDto.setTags(Set.of("tag1"));

        Task taskToSave = new Task();
        taskToSave.setTitle("New Task");

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle("New Task");

        TaskResponseDto responseDto = new TaskResponseDto();
        responseDto.setId(1L);
        responseDto.setTitle("New Task");
        responseDto.setDescription("Description");

        given(taskMapper.toEntity(any(TaskCreateDto.class))).willReturn(taskToSave);
        given(taskService.createTask(taskToSave)).willReturn(savedTask);
        given(taskMapper.toResponseDto(savedTask)).willReturn(responseDto);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    void testGetTask() throws Exception {
        Long taskId = 1L;
        Task task = new Task(
                taskId,
                "Existing Task",
                "Description",
                false,
                LocalDateTime.now(),
                LocalDate.now().plusDays(2),
                Priority.MEDIUM,
                Set.of("tag1")
        );

        TaskResponseDto responseDto = new TaskResponseDto();
        responseDto.setId(taskId);
        responseDto.setTitle("Existing Task");
        responseDto.setDescription("Description");

        given(taskService.getTaskOrThrow(taskId)).willReturn(task);
        given(taskMapper.toResponseDto(task)).willReturn(responseDto);

        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Existing Task"));
    }
}
