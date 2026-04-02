package com.mipt.mvpmts2.repository;

import com.mipt.mvpmts2.model.TaskAttachment;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTaskAttachmentRepository implements TaskAttachmentRepository {

  private final Map<Long, TaskAttachment> storage = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public TaskAttachment save(TaskAttachment attachment) {
    TaskAttachment toStore = copy(attachment);
    if (toStore.getId() == null) {
      toStore.setId(idGenerator.getAndIncrement());
    }
    storage.put(toStore.getId(), toStore);
    return copy(toStore);
  }

  @Override
  public Optional<TaskAttachment> findById(Long id) {
    return Optional.ofNullable(storage.get(id)).map(this::copy);
  }

  @Override
  public List<TaskAttachment> findByTaskId(Long taskId) {
    return storage.values().stream()
        .filter(attachment -> attachment.getTaskId().equals(taskId))
        .sorted(Comparator.comparing(TaskAttachment::getId))
        .map(this::copy)
        .toList();
  }

  @Override
  public void deleteById(Long id) {
    storage.remove(id);
  }

  public void clear() {
    storage.clear();
    idGenerator.set(1);
  }

  private TaskAttachment copy(TaskAttachment attachment) {
    return new TaskAttachment(
        attachment.getId(),
        attachment.getTaskId(),
        attachment.getFileName(),
        attachment.getStoredFileName(),
        attachment.getContentType(),
        attachment.getSize(),
        attachment.getUploadedAt()
    );
  }
}
