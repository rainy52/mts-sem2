package com.mipt.mvpmts2.repository;

import com.mipt.mvpmts2.model.TaskAttachment;
import java.util.List;
import java.util.Optional;

public interface TaskAttachmentRepository {
  TaskAttachment save(TaskAttachment attachment);

  Optional<TaskAttachment> findById(Long id);

  List<TaskAttachment> findByTaskId(Long taskId);

  void deleteById(Long id);
}
