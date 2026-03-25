package com.mipt.mvpmts2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI todoListOpenApi(@Value("${api.version}") String apiVersion) {
    return new OpenAPI().info(new Info()
        .title("To-Do List API")
        .version(apiVersion)
        .description("API for managing tasks, attachments, favorites and user preferences.")
        .contact(new Contact()
            .name("MTS Semester Project")
            .email("support@example.com")));
  }
}
