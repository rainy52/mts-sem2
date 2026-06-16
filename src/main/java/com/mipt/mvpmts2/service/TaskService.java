package com.mipt.mvpmts2.service;

import com.mipt.mvpmts2.model.Priority;
import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.repository.TaskRepository;
import com.mipt.mvpmts2.scope.PrototypeScopedBean;
import com.mipt.mvpmts2.scope.RequestScopedBean;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

@Service
public class TaskService {

  private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

  private final TaskRepository taskRepository;
  private final ObjectProvider<RequestScopedBean> requestScopedBeanProvider;
  private final ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider;
  private final String appName;
  private final String appVersion;
  private final String environment;
  private final boolean debugMode;
  private final boolean loadDefaultTasks;

  public TaskService(
      TaskRepository taskRepository,
      ObjectProvider<RequestScopedBean> requestScopedBeanProvider,
      ObjectProvider<PrototypeScopedBean> prototypeScopedBeanProvider,
      @Value("${app.name}") String appName,
      @Value("${app.version}") String appVersion,
      @Value("${app.environment}") String environment,
      @Value("${app.debug-mode}") boolean debugMode,
      @Value("${app.load-default-tasks:true}") boolean loadDefaultTasks) {
    this.taskRepository = taskRepository;
    this.requestScopedBeanProvider = requestScopedBeanProvider;
    this.prototypeScopedBeanProvider = prototypeScopedBeanProvider;
    this.appName = appName;
    this.appVersion = appVersion;
    this.environment = environment;
    this.debugMode = debugMode;
    this.loadDefaultTasks = loadDefaultTasks;
  }

  @PostConstruct
  public void initializeDefaultTasks() {
    logger.info("Initializing default tasks for {} v{} in {} profile.", appName, appVersion, environment);

    if (taskRepository.count() == 0 && loadDefaultTasks) {
      createDefaultTask("Prepare MVP", "Create the first task manager prototype.");
      createDefaultTask("Review Spring DI", "Verify primary and qualified repositories.");
    }

    logger.info("Default tasks initialized. Debug mode: {}", debugMode);
  }

  @PreDestroy
  public void cleanup() {
    logger.info("Cleaning up TaskService.");
    saveStatisticsToFile();
  }

  public List<Task> getAllTasks() {
    List<Task> tasks = taskRepository.findAll();
    currentRequestScopedBean()
        .ifPresent(bean -> logger.info("Processing getAllTasks for request {}", bean.getRequestId()));
    return tasks;
  }

  public Optional<Task> getTask(Long id) {
    return taskRepository.findById(id);
  }

  public Task getTaskOrThrow(Long id) {
    return getTask(id).orElseThrow(() -> new TaskNotFoundException(id));
  }

  public List<Task> getTasksByIds(Collection<Long> ids) {
    return ids.stream()
        .map(this::getTask)
        .flatMap(Optional::stream)
        .toList();
  }

  public Task createTask(Task task) {
    Task taskToCreate = new Task(
        task.getId() == null ? generateTaskId() : task.getId(),
        task.getTitle(),
        task.getDescription(),
        task.isCompleted(),
        LocalDateTime.now(),
        task.getDueDate(),
        task.getPriority(),
        task.getTags()
    );
    validateDueDate(taskToCreate);
    Task savedTask = taskRepository.save(taskToCreate);
    if (savedTask == null) {
      throw new IllegalStateException("Task repository did not return a saved task.");
    }
    logger.info("Created task {}.", savedTask.getId());
    return savedTask;
  }

  public Task updateTask(Long id, Task task) {
    Task existingTask = getTaskOrThrow(id);
    Task updatedTask = new Task(
        id,
        task.getTitle(),
        task.getDescription(),
        task.isCompleted(),
        existingTask.getCreatedAt(),
        task.getDueDate(),
        task.getPriority(),
        task.getTags()
    );
    validateDueDate(updatedTask);
    updatedTask = taskRepository.save(updatedTask);
    logger.info("Updated task {}.", id);
    return updatedTask;
  }

  @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED, isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED, rollbackFor = TaskNotFoundException.class)
  public void bulkCompleteTasks(List<Long> ids) {
    for (Long id : ids) {
      Task task = getTaskOrThrow(id);
      task.setCompleted(true);
      taskRepository.save(task);
    }
  }

  public List<Task> getTasksWithAttachments() {
    return taskRepository.findAllWithAttachments();
  }


  public void deleteTask(Long id) {
    getTaskOrThrow(id);
    taskRepository.deleteById(id);
    logger.info("Deleted task {}.", id);
  }

  public AppInfo getAppInfo() {
    return new AppInfo(appName, appVersion, environment, debugMode, loadDefaultTasks);
  }

  public CacheStatistics getCacheStatistics() {
    long completedTasks = countCompleted();
    long totalTasks = taskRepository.count();
    return new CacheStatistics(
        (int) totalTasks,
        (int) completedTasks,
        (int) (totalTasks - completedTasks)
    );
  }

  public RequestInfo getRequestInfo() {
    RequestScopedBean requestScopedBean = currentRequestScopedBean()
        .orElseThrow(() -> new IllegalStateException("Request scope is not active."));
    return new RequestInfo(
        requestScopedBean.getRequestId(),
        requestScopedBean.getRequestStartTime(),
        requestScopedBean.getRequestDurationMillis()
    );
  }

  public PrototypeInfo getPrototypeInfo() {
    PrototypeScopedBean prototypeScopedBean = prototypeScopedBeanProvider.getObject();
    return new PrototypeInfo(
        prototypeScopedBean.getInstanceId(),
        prototypeScopedBean.getCreatedAt(),
        prototypeScopedBean.generateTaskId()
    );
  }

  public void clearCacheForTesting() {
  }

  private void createDefaultTask(String title, String description) {
    Task defaultTask = new Task(
        generateTaskId(),
        title,
        description,
        false,
        LocalDateTime.now(),
        LocalDate.now().plusDays(7),
        Priority.MEDIUM,
        Set.of("default")
    );
    taskRepository.save(defaultTask);
  }

  private Long generateTaskId() {
    return prototypeScopedBeanProvider.getObject().generateTaskId();
  }


  private void validateDueDate(Task task) {
    if (task.getDueDate() != null
        && task.getCreatedAt() != null
        && task.getDueDate().isBefore(task.getCreatedAt().toLocalDate())) {
      throw new IllegalArgumentException("Due date must not be earlier than the task creation date.");
    }
  }

  private long countCompleted() {
    return taskRepository.findAll().stream()
        .filter(Task::isCompleted)
        .count();
  }

  private Optional<RequestScopedBean> currentRequestScopedBean() {
    if (RequestContextHolder.getRequestAttributes() == null) {
      return Optional.empty();
    }
    return Optional.of(requestScopedBeanProvider.getObject());
  }

  private void saveStatisticsToFile() {
    Path logDirectory = Path.of("logs");
    Path statisticsFile = logDirectory.resolve("task-service-shutdown.txt");
    String content = String.join(System.lineSeparator(),
        "application=" + appName,
        "version=" + appVersion,
        "environment=" + environment,
        "timestamp=" + LocalDateTime.now(),
        "totalTasks=" + taskRepository.count()
    );
    try {
      Files.createDirectories(logDirectory);
      Files.writeString(statisticsFile, content);
    } catch (IOException exception) {
      logger.error("Could not save task service statistics.", exception);
    }
  }

  public record AppInfo(
      String name,
      String version,
      String environment,
      boolean debugMode,
      boolean loadDefaultTasks) {
  }

  public record CacheStatistics(int total, int completed, int active) {
  }

  public record RequestInfo(String requestId, LocalDateTime requestStartTime, long durationMillis) {
  }

  public record PrototypeInfo(String instanceId, LocalDateTime createdAt, Long generatedTaskId) {
  }
}
