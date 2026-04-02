package com.mipt.mvpmts2.service;

/**
 * Signals that a task does not exist.
 */
public class TaskNotFoundException extends RuntimeException {

  public TaskNotFoundException(Long taskId) {
    super("Task with id " + taskId + " was not found.");
  }
}
