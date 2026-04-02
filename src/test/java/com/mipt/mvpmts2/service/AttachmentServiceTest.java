package com.mipt.mvpmts2.service;

import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.repository.InMemoryTaskAttachmentRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AttachmentServiceTest {

  @TempDir
  Path tempDir;

  private AttachmentService attachmentService;
  private InMemoryTaskAttachmentRepository attachmentRepository;
  private TaskService taskService;

  @BeforeEach
  void setUp() {
    attachmentRepository = new InMemoryTaskAttachmentRepository();
    taskService = mock(TaskService.class);
    attachmentService = new AttachmentService(attachmentRepository, taskService, tempDir.toString());
    attachmentService.initializeStorage();
  }

  @Test
  void storeLoadAndDeleteAttachmentLifecycle() throws Exception {
    when(taskService.getTaskOrThrow(1L)).thenReturn(new Task());
    MockMultipartFile file = new MockMultipartFile("file", "report.txt", "text/plain", "report".getBytes());

    var attachment = attachmentService.storeAttachment(1L, file);

    assertNotNull(attachment.getId());
    assertEquals("report.txt", attachment.getFileName());
    assertTrue(Files.exists(tempDir.resolve(attachment.getStoredFileName())));
    try (var inputStream = attachmentService.loadAsResource(attachment.getId()).getInputStream()) {
      assertEquals("report", new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
    }

    attachmentService.deleteAttachment(attachment.getId());

    assertFalse(Files.exists(tempDir.resolve(attachment.getStoredFileName())));
    assertThrows(AttachmentNotFoundException.class, () -> attachmentService.getAttachment(attachment.getId()));
  }

  @Test
  void storeAttachmentRejectsEmptyFile() {
    when(taskService.getTaskOrThrow(1L)).thenReturn(new Task());
    MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

    assertThrows(IllegalArgumentException.class, () -> attachmentService.storeAttachment(1L, file));
  }
}
