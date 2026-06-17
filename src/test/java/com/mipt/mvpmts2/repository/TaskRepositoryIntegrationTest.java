package com.mipt.mvpmts2.repository;

import com.mipt.mvpmts2.model.Priority;
import com.mipt.mvpmts2.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testFindTasksDueWithinDays() {
        Task taskDueSoon = new Task(
                null,
                "Due Soon",
                "Description",
                false,
                LocalDateTime.now(),
                LocalDate.now().plusDays(2),
                Priority.HIGH,
                Set.of("urgent")
        );
        taskRepository.save(taskDueSoon);

        Task taskDueLater = new Task(
                null,
                "Due Later",
                "Description",
                false,
                LocalDateTime.now(),
                LocalDate.now().plusDays(10),
                Priority.LOW,
                Set.of("someday")
        );
        taskRepository.save(taskDueLater);

        List<Task> result = taskRepository.findTasksDueWithinDays(LocalDate.now().plusDays(5));

        assertEquals(1, result.size());
        assertEquals("Due Soon", result.get(0).getTitle());
    }
}
