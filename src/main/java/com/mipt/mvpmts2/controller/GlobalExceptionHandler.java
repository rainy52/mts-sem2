package com.mipt.mvpmts2.controller;

import com.mipt.mvpmts2.service.TaskNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Converts application exceptions into consistent HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleTaskNotFoundException(
      TaskNotFoundException exception,
      HttpServletRequest request) {
    return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception,
      HttpServletRequest request) {
    String message = exception.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.joining("; "));
    return buildResponse(HttpStatus.BAD_REQUEST, message, request);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException exception,
      HttpServletRequest request) {
    String message = "Invalid value for parameter '" + exception.getName() + "'.";
    return buildResponse(HttpStatus.BAD_REQUEST, message, request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException exception,
      HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, "Request body is missing or malformed.", request);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException exception,
      HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiErrorResponse> handleRuntimeException(
      RuntimeException exception,
      HttpServletRequest request) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request);
  }

  private ResponseEntity<ApiErrorResponse> buildResponse(
      HttpStatus status,
      String message,
      HttpServletRequest request) {
    ApiErrorResponse response = new ApiErrorResponse(
        LocalDateTime.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI()
    );
    return ResponseEntity.status(status).body(response);
  }
}
