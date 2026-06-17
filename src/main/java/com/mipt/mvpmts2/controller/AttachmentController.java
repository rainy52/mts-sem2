package com.mipt.mvpmts2.controller;

import com.mipt.mvpmts2.dto.AttachmentResponseDto;
import com.mipt.mvpmts2.dto.ErrorResponse;
import com.mipt.mvpmts2.model.TaskAttachment;
import com.mipt.mvpmts2.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Attachments", description = "Task attachment operations.")
public class AttachmentController {

  private final AttachmentService attachmentService;

  public AttachmentController(AttachmentService attachmentService) {
    this.attachmentService = attachmentService;
  }

  @Operation(summary = "Upload an attachment for a task")
  @ApiResponse(responseCode = "201", description = "Attachment uploaded",
      content = @Content(schema = @Schema(implementation = AttachmentResponseDto.class)))
  @ApiResponse(responseCode = "400", description = "Invalid file",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Task not found",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping(value = "/api/tasks/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<AttachmentResponseDto> uploadAttachment(
      @PathVariable Long taskId,
      @RequestParam("file") MultipartFile file) {
    TaskAttachment attachment = attachmentService.storeAttachment(taskId, file);
    return ResponseEntity.created(URI.create("/api/attachments/" + attachment.getId()))
        .body(attachmentService.toResponseDto(attachment));
  }

  @Operation(summary = "Download an attachment")
  @ApiResponse(responseCode = "200", description = "Attachment downloaded")
  @ApiResponse(responseCode = "404", description = "Attachment not found",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @GetMapping("/api/attachments/{attachmentId}")
  public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
    TaskAttachment attachment = attachmentService.getAttachment(attachmentId);
    Resource resource = attachmentService.loadAsResource(attachmentId);
    MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
    if (attachment.getContentType() != null) {
      contentType = MediaType.parseMediaType(attachment.getContentType());
    }

    return ResponseEntity.ok()
        .contentType(contentType)
        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
            .filename(attachment.getFileName())
            .build()
            .toString())
        .body(resource);
  }

  @Operation(summary = "Delete an attachment")
  @ApiResponse(responseCode = "204", description = "Attachment deleted")
  @ApiResponse(responseCode = "404", description = "Attachment not found",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @DeleteMapping("/api/attachments/{attachmentId}")
  public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
    attachmentService.deleteAttachment(attachmentId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "List attachments for a task")
  @ApiResponse(responseCode = "200", description = "Attachments returned",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = AttachmentResponseDto.class))))
  @ApiResponse(responseCode = "404", description = "Task not found",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @GetMapping("/api/tasks/{taskId}/attachments")
  public ResponseEntity<List<AttachmentResponseDto>> listAttachments(@PathVariable Long taskId) {
    List<AttachmentResponseDto> body = attachmentService.getTaskAttachments(taskId).stream()
        .map(attachmentService::toResponseDto)
        .toList();
    return ResponseEntity.ok(body);
  }
}
