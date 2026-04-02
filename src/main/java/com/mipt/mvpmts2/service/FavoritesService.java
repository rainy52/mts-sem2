package com.mipt.mvpmts2.service;

import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class FavoritesService {

  public static final String FAVORITES_SESSION_KEY = "favoriteTaskIds";

  public void addFavorite(HttpSession session, Long taskId) {
    favoriteTaskIds(session).add(taskId);
  }

  public void removeFavorite(HttpSession session, Long taskId) {
    favoriteTaskIds(session).remove(taskId);
  }

  public Set<Long> getFavoriteTaskIds(HttpSession session) {
    return new LinkedHashSet<>(favoriteTaskIds(session));
  }

  @SuppressWarnings("unchecked")
  private Set<Long> favoriteTaskIds(HttpSession session) {
    Object attribute = session.getAttribute(FAVORITES_SESSION_KEY);
    if (attribute instanceof Set<?> ids) {
      return (Set<Long>) ids;
    }

    Set<Long> ids = new LinkedHashSet<>();
    session.setAttribute(FAVORITES_SESSION_KEY, ids);
    return ids;
  }
}
