package com.mipt.mvpmts2.config;

import com.mipt.mvpmts2.repository.StubTaskRepository;
import com.mipt.mvpmts2.repository.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers repository beans
 */
@Configuration
public class RepositoryConfig {

  @Bean("stubTaskRepository")
  public TaskRepository stubTaskRepository() {
    return new StubTaskRepository();
  }
}
