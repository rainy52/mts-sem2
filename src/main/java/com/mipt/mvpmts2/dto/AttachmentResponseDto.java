package com.mipt.mvpmts2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Attachment metadata returned to API consumers.")
public class AttachmentResponseDto {
  @Schema(description = "Attachment identifier.", example = "5")
  private Long id;

  @Schema(description = "Original file name.", example = "specification.pdf")
  private String fileName;

  @Schema(description = "Attachment size in bytes.", example = "2048")
  private long size;

  @Schema(description = "Attachment upload time.", example = "2026-03-24T21:00:00")
  private LocalDateTime uploadedAt;

  public AttachmentResponseDto() {
  }

  public AttachmentResponseDto(Long id, String fileName, long size, LocalDateTime uploadedAt) {
    this.id = id;
    this.fileName = fileName;
    this.size = size;
    this.uploadedAt = uploadedAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public LocalDateTime getUploadedAt() {
    return uploadedAt;
  }

  public void setUploadedAt(LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
  }
}
