package com.mipt.mvpmts2.service;

import com.mipt.mvpmts2.dto.AttachmentResponseDto;
import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.model.TaskAttachment;
import com.mipt.mvpmts2.repository.TaskAttachmentRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentService {

  private final TaskAttachmentRepository attachmentRepository;
  private final TaskService taskService;
  private final Path uploadDirectory;

  public AttachmentService(
      TaskAttachmentRepository attachmentRepository,
      TaskService taskService,
      @Value("${app.attachments.upload-dir:uploads}") String uploadDirectory) {
    this.attachmentRepository = attachmentRepository;
    this.taskService = taskService;
    this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
  }

  @PostConstruct
  public void initializeStorage() {
    try {
      Files.createDirectories(uploadDirectory);
    } catch (IOException exception) {
      throw new IllegalStateException("Could not initialize attachment storage.", exception);
    }
  }

  public TaskAttachment storeAttachment(Long taskId, MultipartFile file) {
    taskService.getTaskOrThrow(taskId);
    validateFile(file);

    String fileName = StringUtils.cleanPath(file.getOriginalFilename());
    String storedFileName = buildStoredFileName(fileName);
    Path target = uploadDirectory.resolve(storedFileName);

    try (InputStream inputStream = file.getInputStream()) {
      Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException exception) {
      throw new IllegalStateException("Could not store attachment.", exception);
    }

    Task task = taskService.getTaskOrThrow(taskId);

    TaskAttachment attachment = new TaskAttachment(
        null,
        task,
        fileName,
        storedFileName,
        file.getContentType(),
        file.getSize(),
        LocalDateTime.now()
    );
    return attachmentRepository.save(attachment);
  }

  public TaskAttachment getAttachment(Long attachmentId) {
    return attachmentRepository.findById(attachmentId)
        .orElseThrow(() -> new AttachmentNotFoundException(attachmentId));
  }

  public Resource loadAsResource(Long attachmentId) {
    TaskAttachment attachment = getAttachment(attachmentId);
    Path file = uploadDirectory.resolve(attachment.getStoredFileName());

    try {
      Resource resource = new UrlResource(file.toUri());
      if (!resource.exists() || !resource.isReadable()) {
        throw new AttachmentNotFoundException(attachmentId);
      }
      return resource;
    } catch (IOException exception) {
      throw new IllegalStateException("Could not read attachment.", exception);
    }
  }

  public void deleteAttachment(Long attachmentId) {
    TaskAttachment attachment = getAttachment(attachmentId);
    try {
      Files.deleteIfExists(uploadDirectory.resolve(attachment.getStoredFileName()));
    } catch (IOException exception) {
      throw new IllegalStateException("Could not delete attachment file.", exception);
    }
    attachmentRepository.deleteById(attachmentId);
  }

  public List<TaskAttachment> getTaskAttachments(Long taskId) {
    taskService.getTaskOrThrow(taskId);
    return attachmentRepository.findByTaskId(taskId);
  }

  public AttachmentResponseDto toResponseDto(TaskAttachment attachment) {
    return new AttachmentResponseDto(
        attachment.getId(),
        attachment.getFileName(),
        attachment.getSize(),
        attachment.getUploadedAt()
    );
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Attachment file must not be empty.");
    }

    String fileName = StringUtils.cleanPath(file.getOriginalFilename());
    if (!StringUtils.hasText(fileName) || fileName.contains("..")) {
      throw new IllegalArgumentException("Attachment file name is invalid.");
    }
  }

  private String buildStoredFileName(String originalFileName) {
    String extension = StringUtils.getFilenameExtension(originalFileName);
    if (StringUtils.hasText(extension)) {
      return UUID.randomUUID() + "." + extension;
    }
    return UUID.randomUUID().toString();
  }
}
