package com.mipt.mvpmts2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

@Schema(description = "Unified API error payload.")
public class ErrorResponse {
  @Schema(description = "Error timestamp.", example = "2026-03-24T20:15:00Z")
  private Instant timestamp;

  @Schema(description = "HTTP status code.", example = "400")
  private int status;

  @Schema(description = "Short HTTP error description.", example = "Bad Request")
  private String error;

  @Schema(description = "Human-readable error message.", example = "Task title must not be blank.")
  private String message;

  @Schema(description = "Request path that caused the error.", example = "/api/tasks")
  private String path;

  @Schema(description = "Additional details, for example field validation errors.")
  private Map<String, Object> details;

  public ErrorResponse() {
  }

  public ErrorResponse(
      Instant timestamp,
      int status,
      String error,
      String message,
      String path,
      Map<String, Object> details) {
    this.timestamp = timestamp;
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
    this.details = details;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Map<String, Object> getDetails() {
    return details;
  }

  public void setDetails(Map<String, Object> details) {
    this.details = details;
  }
}
