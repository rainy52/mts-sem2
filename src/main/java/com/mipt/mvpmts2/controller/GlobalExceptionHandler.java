package com.mipt.mvpmts2.controller;

import com.mipt.mvpmts2.dto.ErrorResponse;
import com.mipt.mvpmts2.service.AttachmentNotFoundException;
import com.mipt.mvpmts2.service.TaskNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private final String environment;

  public GlobalExceptionHandler(@Value("${app.environment}") String environment) {
    this.environment = environment;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception,
      HttpServletRequest request) {
    Map<String, Object> details = new LinkedHashMap<>();
    Map<String, List<String>> fieldErrors = exception.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(Collectors.groupingBy(
            FieldError::getField,
            LinkedHashMap::new,
            Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
        ));
    details.put("fieldErrors", fieldErrors);
    return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed.", request, details, exception);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException exception,
      HttpServletRequest request) {
    Map<String, Object> details = new LinkedHashMap<>();
    Map<String, String> violations = exception.getConstraintViolations()
        .stream()
        .collect(Collectors.toMap(
            violation -> violation.getPropertyPath().toString(),
            ConstraintViolation::getMessage,
            (left, right) -> right,
            LinkedHashMap::new
        ));
    details.put("violations", violations);
    return buildResponse(HttpStatus.BAD_REQUEST, "Constraint validation failed.", request, details, exception);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException exception,
      HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, Map.of(), exception);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException exception,
      HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, "Request body is missing or malformed.", request, Map.of(), exception);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
      NoHandlerFoundException exception,
      HttpServletRequest request) {
    return buildResponse(HttpStatus.NOT_FOUND, "No handler found for the requested path.", request, Map.of(), exception);
  }

  @ExceptionHandler({TaskNotFoundException.class, AttachmentNotFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFoundException(RuntimeException exception, HttpServletRequest request) {
    return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of(), exception);
  }

  @ExceptionHandler({
      IllegalArgumentException.class,
      MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ErrorResponse> handleBadRequestException(Exception exception, HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, Map.of(), exception);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
    Map<String, Object> details = new LinkedHashMap<>();
    if (!"production".equalsIgnoreCase(environment)) {
      details.put("exception", exception.getClass().getSimpleName());
    }
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request, details, exception);
  }

  private ResponseEntity<ErrorResponse> buildResponse(
      HttpStatus status,
      String message,
      HttpServletRequest request,
      Map<String, Object> details,
      Exception exception) {
    if (status.is5xxServerError()) {
      logger.error("Unhandled exception for {}", request.getRequestURI(), exception);
    } else {
      logger.warn("Handled {} for {}: {}", status, request.getRequestURI(), message);
    }
    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI(),
        details
    );
    return ResponseEntity.status(status).body(response);
  }
}
