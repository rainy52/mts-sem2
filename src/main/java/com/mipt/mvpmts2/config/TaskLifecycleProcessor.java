package com.mipt.mvpmts2.config;

import com.mipt.mvpmts2.repository.TaskRepository;
import com.mipt.mvpmts2.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class TaskLifecycleProcessor implements BeanPostProcessor {

  private static final Logger logger = LoggerFactory.getLogger(TaskLifecycleProcessor.class);

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    if (isTrackedBean(bean)) {
      logger.info(
          "Bean '{}' of type '{}' was created and is about to be initialized.",
          beanName,
          bean.getClass().getSimpleName()
      );
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (isTrackedBean(bean)) {
      logger.info(
          "Bean '{}' of type '{}' has been initialized.",
          beanName,
          bean.getClass().getSimpleName()
      );
    }
    return bean;
  }

  private boolean isTrackedBean(Object bean) {
    return bean instanceof TaskService || bean instanceof TaskRepository;
  }
}
