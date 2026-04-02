package com.mipt.mvpmts2.service;

import jakarta.servlet.http.HttpSession;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FavoritesServiceTest {

  private final FavoritesService favoritesService = new FavoritesService();

  @Test
  void addAndRemoveFavoriteIdsInSession() {
    HttpSession session = new MockHttpSession();

    favoritesService.addFavorite(session, 1L);
    favoritesService.addFavorite(session, 2L);
    favoritesService.removeFavorite(session, 1L);

    assertEquals(Set.of(2L), favoritesService.getFavoriteTaskIds(session));
  }
}
