package com.mipt.mvpmts2.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current view preference stored in a cookie.")
public class ViewPreferenceDto {
  @Schema(description = "Preferred task list mode.", example = "compact")
  private String mode;

  public ViewPreferenceDto() {
  }

  public ViewPreferenceDto(String mode) {
    this.mode = mode;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }
}
