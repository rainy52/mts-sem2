package com.mipt.mvpmts2.controller;

import com.mipt.mvpmts2.model.Priority;
import com.mipt.mvpmts2.model.Task;
import com.mipt.mvpmts2.repository.InMemoryTaskRepository;
import com.mipt.mvpmts2.service.TaskService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FavoritesAndPreferencesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private InMemoryTaskRepository taskRepository;

  @Autowired
  private TaskService taskService;

  @BeforeEach
  void setUp() {
    taskRepository.clear();
    taskService.clearCacheForTesting();
  }

  @Test
  void addAndListFavoritesUseSession() throws Exception {
    Task task = createTask("Favorite task");
    MockHttpSession session = new MockHttpSession();

    mockMvc.perform(post("/api/favorites/{taskId}", task.getId()).session(session))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/favorites").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(task.getId()))
        .andExpect(jsonPath("$[0].title").value("Favorite task"));
  }

  @Test
  void addFavoriteForMissingTaskReturns404() throws Exception {
    mockMvc.perform(post("/api/favorites/999").session(new MockHttpSession()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Task with id 999 was not found."));
  }

  @Test
  void removeFavoriteReturnsNoContent() throws Exception {
    Task task = createTask("Remove favorite");
    MockHttpSession session = new MockHttpSession();

    mockMvc.perform(post("/api/favorites/{taskId}", task.getId()).session(session))
        .andExpect(status().isOk());

    mockMvc.perform(delete("/api/favorites/{taskId}", task.getId()).session(session))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/favorites").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void getViewPreferenceSetsDefaultCookie() throws Exception {
    mockMvc.perform(get("/api/preferences/view"))
        .andExpect(status().isOk())
        .andExpect(cookie().value("viewPreference", "detailed"))
        .andExpect(jsonPath("$.mode").value("detailed"));
  }

  @Test
  void updateViewPreferenceSetsCookie() throws Exception {
    mockMvc.perform(post("/api/preferences/view").param("mode", "compact"))
        .andExpect(status().isOk())
        .andExpect(cookie().value("viewPreference", "compact"))
        .andExpect(jsonPath("$.mode").value("compact"));
  }

  @Test
  void updateViewPreferenceRejectsInvalidMode() throws Exception {
    mockMvc.perform(post("/api/preferences/view").param("mode", "grid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("View mode must be either 'compact' or 'detailed'."));
  }

  private Task createTask(String title) {
    return taskRepository.save(new Task(
        null,
        title,
        "Description",
        false,
        LocalDateTime.now(),
        LocalDate.now().plusDays(2),
        Priority.MEDIUM,
        Set.of("favorite")
    ));
  }
}
