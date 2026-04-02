package com.mipt.mvpmts2.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class JsonTestUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private JsonTestUtils() {
  }

  static long readLong(String json, String fieldName) throws Exception {
    JsonNode root = OBJECT_MAPPER.readTree(json);
    return root.get(fieldName).asLong();
  }
}
