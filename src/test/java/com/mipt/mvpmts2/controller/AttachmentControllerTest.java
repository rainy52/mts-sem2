package com.mipt.mvpmts2.controller;

import com.mipt.mvpmts2.model.Priority;
import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.repository.TaskAttachmentRepository;
import com.mipt.mvpmts2.repository.TaskRepository;
import com.mipt.mvpmts2.service.TaskService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AttachmentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskAttachmentRepository attachmentRepository;

  @Autowired
  private TaskService taskService;

  @Value("${app.attachments.upload-dir}")
  private String uploadDir;

  @BeforeEach
  void setUp() throws IOException {
    attachmentRepository.deleteAll();
    taskRepository.deleteAll();
    taskService.clearCacheForTesting();
    Path uploadPath = Path.of(uploadDir);
    if (Files.exists(uploadPath)) {
      try (var stream = Files.walk(uploadPath)) {
        stream.sorted(Comparator.reverseOrder())
            .filter(path -> !path.equals(uploadPath))
            .forEach(path -> {
              try {
                Files.deleteIfExists(path);
              } catch (IOException exception) {
                throw new RuntimeException(exception);
              }
            });
      }
    }
    Files.createDirectories(uploadPath);
  }

  @Test
  void uploadAndListAttachmentsWork() throws Exception {
    Task task = createTask("Task with attachment");
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "notes.txt",
        "text/plain",
        "hello attachment".getBytes()
    );

    mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", task.getId()).file(file))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.fileName").value("notes.txt"))
        .andExpect(jsonPath("$.size").value(file.getSize()));

    mockMvc.perform(get("/api/tasks/{taskId}/attachments", task.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].fileName").value("notes.txt"));
  }

  @Test
  void uploadRejectsEmptyFile() throws Exception {
    Task task = createTask("Task with empty file");
    MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

    mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", task.getId()).file(file))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Attachment file must not be empty."));
  }

  @Test
  void downloadAttachmentStreamsFileContent() throws Exception {
    Task task = createTask("Task for download");
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "download.txt",
        "text/plain",
        "download content".getBytes()
    );

    String response = mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", task.getId()).file(file))
        .andReturn()
        .getResponse()
        .getContentAsString();
    long attachmentId = JsonTestUtils.readLong(response, "id");

    mockMvc.perform(get("/api/attachments/{attachmentId}", attachmentId))
        .andExpect(status().isOk())
        .andExpect(content().string("download content"));
  }

  @Test
  void deleteMissingAttachmentReturns404() throws Exception {
    mockMvc.perform(delete("/api/attachments/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Attachment with id 999 was not found."));
  }

  @Test
  void listAttachmentsForMissingTaskReturns404() throws Exception {
    mockMvc.perform(get("/api/tasks/999/attachments"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Task with id 999 was not found."));
  }

  private Task createTask(String title) {
    return taskRepository.save(new Task(
        null,
        title,
        "Description",
        false,
        LocalDateTime.now(),
        LocalDate.now().plusDays(2),
        Priority.MEDIUM,
        Set.of("attachment")
    ));
  }
}
