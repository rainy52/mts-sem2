package com.mipt.mvpmts2.service;

import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.repository.TaskRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TaskStatisticsService {

  private final TaskRepository primaryRepository;

  public TaskStatisticsService(TaskRepository primaryRepository) {
    this.primaryRepository = primaryRepository;
  }

  public StatisticsResult compareRepositories() {
    List<Task> primaryTasks = primaryRepository.findAll();

    return new StatisticsResult(
        "primary",
        primaryTasks.size(),
        primaryTasks
    );
  }

  public record StatisticsResult(
      String primaryRepo,
      int primaryCount,
      List<Task> primaryTasks) {
  }
}
