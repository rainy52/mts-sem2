package com.mipt.mvpmts2.repository;

import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.model.Priority;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCompletedAndPriority(boolean completed, Priority priority);

    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN CURRENT_DATE AND :endDate")
    List<Task> findTasksDueWithinDays(@Param("endDate") LocalDate endDate);

    @EntityGraph(attributePaths = {"attachments"})
    @Query("SELECT t FROM Task t")
    List<Task> findAllWithAttachments();
}
