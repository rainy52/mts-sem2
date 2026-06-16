package com.mipt.mvpmts2.controller;

import com.mipt.mvpmts2.dto.ErrorResponse;
import com.mipt.mvpmts2.dto.ViewPreferenceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preferences")
@Tag(name = "Preferences", description = "Cookie-based user preferences.")
public class PreferencesController {

  private static final String COOKIE_NAME = "viewPreference";
  private static final String DEFAULT_MODE = "detailed";

  @Operation(summary = "Read current view preference")
  @ApiResponse(responseCode = "200", description = "View preference returned")
  @GetMapping("/view")
  public ResponseEntity<ViewPreferenceDto> getViewPreference(
      @CookieValue(name = COOKIE_NAME, required = false) String currentMode) {
    String resolvedMode = normalizeMode(currentMode);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, buildCookie(resolvedMode).toString())
        .body(new ViewPreferenceDto(resolvedMode));
  }

  @Operation(summary = "Update current view preference")
  @ApiResponse(responseCode = "200", description = "View preference updated")
  @ApiResponse(responseCode = "400", description = "Invalid mode",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/view")
  public ResponseEntity<ViewPreferenceDto> updateViewPreference(@RequestParam String mode) {
    String resolvedMode = normalizeMode(mode);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, buildCookie(resolvedMode).toString())
        .body(new ViewPreferenceDto(resolvedMode));
  }

  private String normalizeMode(String mode) {
    if (mode == null || mode.isBlank()) {
      return DEFAULT_MODE;
    }

    if (!"compact".equals(mode) && !"detailed".equals(mode)) {
      throw new IllegalArgumentException("View mode must be either 'compact' or 'detailed'.");
    }
    return mode;
  }

  private ResponseCookie buildCookie(String mode) {
    return ResponseCookie.from(COOKIE_NAME, mode)
        .path("/")
        .httpOnly(false)
        .sameSite("Lax")
        .build();
  }
}
