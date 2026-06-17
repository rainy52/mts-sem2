package com.mts.gateway.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ProfileController {

    @GetMapping("/profile")
    public ResponseEntity<Map<String, String>> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, String> response = new HashMap<>();
        response.put("username", auth.getName());
        response.put("message", "Profile accessed successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/docs")
    public ResponseEntity<Map<String, String>> getDocs() {
        Map<String, String> response = new HashMap<>();
        response.put("docs", "Here are the secret documents");
        return ResponseEntity.ok(response);
    }
}
