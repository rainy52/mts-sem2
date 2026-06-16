package com.mipt.mvpmts2.repository;

import com.mipt.mvpmts2.model.Priority;
import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.model.TaskAttachment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAttachmentRepository taskAttachmentRepository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    @Test
    void testSaveTaskWithAttachment() {
        Task task = new Task(null, "Test Title", "Test Desc", false, LocalDateTime.now(), LocalDate.now().plusDays(1), Priority.HIGH, Set.of("tag1"));
        Task savedTask = taskRepository.save(task);

        TaskAttachment attachment = new TaskAttachment(null, savedTask, "file.txt", "stored.txt", "text/plain", 100, LocalDateTime.now());
        taskAttachmentRepository.save(attachment);

        entityManager.flush();
        entityManager.clear();

        List<Task> tasksWithAttachments = taskRepository.findAllWithAttachments();
        assertThat(tasksWithAttachments).hasSize(1);
        assertThat(tasksWithAttachments.get(0).getAttachments()).hasSize(1);
    }

    @Test
    void testFindTasksDueWithinDays() {
        Task task1 = new Task(null, "Task 1", "Desc 1", false, LocalDateTime.now(), LocalDate.now().plusDays(2), Priority.MEDIUM, Set.of());
        Task task2 = new Task(null, "Task 2", "Desc 2", false, LocalDateTime.now(), LocalDate.now().plusDays(10), Priority.MEDIUM, Set.of());
        taskRepository.save(task1);
        taskRepository.save(task2);

        List<Task> tasksDue = taskRepository.findTasksDueWithinDays(LocalDate.now().plusDays(7));

        assertThat(tasksDue).hasSize(1);
        assertThat(tasksDue.get(0).getTitle()).isEqualTo("Task 1");
    }

    @Test
    void testFindByCompletedAndPriority() {
        Task task1 = new Task(null, "Task 1", "Desc 1", true, LocalDateTime.now(), LocalDate.now(), Priority.HIGH, Set.of());
        Task task2 = new Task(null, "Task 2", "Desc 2", false, LocalDateTime.now(), LocalDate.now(), Priority.HIGH, Set.of());
        taskRepository.save(task1);
        taskRepository.save(task2);

        List<Task> tasks = taskRepository.findByCompletedAndPriority(true, Priority.HIGH);
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Task 1");
    }
}
