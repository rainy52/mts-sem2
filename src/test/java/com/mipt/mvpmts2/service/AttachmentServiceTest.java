package com.mipt.mvpmts2.service;

import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.repository.TaskAttachmentRepository;
import com.mipt.mvpmts2.repository.TaskRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class AttachmentServiceTest {

  @Value("${app.attachments.upload-dir}")
  private String uploadDir;

  @Autowired
  private AttachmentService attachmentService;

  @Autowired
  private TaskAttachmentRepository attachmentRepository;

  @Autowired
  private TaskRepository taskRepository;

  @BeforeEach
  void setUp() {
    attachmentRepository.deleteAll();
    taskRepository.deleteAll();
    attachmentService.initializeStorage();
  }

  @Test
  void storeLoadAndDeleteAttachmentLifecycle() throws Exception {
    Task task = taskRepository.save(new Task(null, "Task", "Desc", false, LocalDateTime.now(), LocalDate.now().plusDays(2), com.mipt.mvpmts2.model.Priority.MEDIUM, java.util.Set.of()));
    MockMultipartFile file = new MockMultipartFile("file", "report.txt", "text/plain", "report".getBytes());

    var attachment = attachmentService.storeAttachment(task.getId(), file);

    assertNotNull(attachment.getId());
    assertEquals("report.txt", attachment.getFileName());
    assertTrue(Files.exists(Path.of(uploadDir).resolve(attachment.getStoredFileName())));
    try (var inputStream = attachmentService.loadAsResource(attachment.getId()).getInputStream()) {
      assertEquals("report", new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
    }

    attachmentService.deleteAttachment(attachment.getId());

    assertFalse(Files.exists(Path.of(uploadDir).resolve(attachment.getStoredFileName())));
    assertThrows(AttachmentNotFoundException.class, () -> attachmentService.getAttachment(attachment.getId()));
  }

  @Test
  void storeAttachmentRejectsEmptyFile() {
    Task task = taskRepository.save(new Task(null, "Task", "Desc", false, LocalDateTime.now(), LocalDate.now().plusDays(2), com.mipt.mvpmts2.model.Priority.MEDIUM, java.util.Set.of()));
    MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

    assertThrows(IllegalArgumentException.class, () -> attachmentService.storeAttachment(task.getId(), file));
  }
}
