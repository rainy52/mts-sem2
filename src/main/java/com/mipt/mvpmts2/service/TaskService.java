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

/**
 * Coordinates task CRUD operations, cache lifecycle and scope-based demonstrations.
 */
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
  private final Map<Long, Task> taskCache = new ConcurrentHashMap<>();

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
  public void initializeCache() {
    logger.info("Initializing task cache for {} v{} in {} profile.", appName, appVersion, environment);
    refreshCache(taskRepository.findAll());

    if (taskCache.isEmpty() && loadDefaultTasks) {
      createDefaultTask("Prepare MVP", "Create the first task manager prototype.");
      createDefaultTask("Review Spring DI", "Verify primary and qualified repositories.");
    }

    logger.info("Task cache initialized with {} task(s). Debug mode: {}", taskCache.size(), debugMode);
  }

  @PreDestroy
  public void cleanup() {
    logger.info("Cleaning up TaskService. {} task(s) remain in cache.", taskCache.size());
    saveStatisticsToFile();
    taskCache.clear();
  }

  public List<Task> getAllTasks() {
    List<Task> tasks = taskRepository.findAll();
    refreshCache(tasks);
    currentRequestScopedBean()
        .ifPresent(bean -> logger.info("Processing getAllTasks for request {}", bean.getRequestId()));
    return tasks;
  }

  public Optional<Task> getTask(Long id) {
    Optional<Task> cachedTask = Optional.ofNullable(taskCache.get(id));
    if (cachedTask.isPresent()) {
      return cachedTask;
    }
    Optional<Task> taskFromRepository = taskRepository.findById(id);
    taskFromRepository.ifPresent(task -> taskCache.put(task.getId(), task));
    return taskFromRepository;
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
    taskCache.put(savedTask.getId(), savedTask);
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
    updatedTask = taskRepository.update(updatedTask);
    taskCache.put(updatedTask.getId(), updatedTask);
    logger.info("Updated task {}.", id);
    return updatedTask;
  }

  public void deleteTask(Long id) {
    getTaskOrThrow(id);
    taskRepository.deleteById(id);
    taskCache.remove(id);
    logger.info("Deleted task {}.", id);
  }

  public AppInfo getAppInfo() {
    return new AppInfo(appName, appVersion, environment, debugMode, loadDefaultTasks);
  }

  public CacheStatistics getCacheStatistics() {
    long completedTasks = countCompleted();
    return new CacheStatistics(
        taskCache.size(),
        (int) completedTasks,
        (int) (taskCache.size() - completedTasks)
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
    taskCache.clear();
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
    Task savedTask = taskRepository.save(defaultTask);
    if (savedTask != null) {
      taskCache.put(savedTask.getId(), savedTask);
    }
  }

  private Long generateTaskId() {
    return prototypeScopedBeanProvider.getObject().generateTaskId();
  }

  private void refreshCache(List<Task> tasks) {
    taskCache.clear();
    List<Task> safeTasks = tasks == null ? Collections.emptyList() : tasks;
    safeTasks.forEach(task -> taskCache.put(task.getId(), task));
  }

  private void validateDueDate(Task task) {
    if (task.getDueDate() != null
        && task.getCreatedAt() != null
        && task.getDueDate().isBefore(task.getCreatedAt().toLocalDate())) {
      throw new IllegalArgumentException("Due date must not be earlier than the task creation date.");
    }
  }

  private long countCompleted() {
    return taskCache.values().stream()
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
        "cachedTasks=" + taskCache.size()
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
