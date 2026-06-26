package com.docgen.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import java.util.Map;

/**
 * Small helper around a shared {@link ObjectMapper} for working with the JSONB
 * (stored as {@code String}) columns used throughout the domain model.
 */
public final class Json {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Json() {}

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /** Serialize any object to a compact JSON string; returns {@code null} for {@code null} input. */
    public static String write(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new Exceptions.BadRequestException("Could not serialize JSON: " + e.getMessage());
        }
    }

    /** Parse a JSON string into a {@link JsonNode}; returns {@link NullNode} for blank input. */
    public static JsonNode read(String json) {
        if (json == null || json.isBlank()) {
            return NullNode.getInstance();
        }
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new Exceptions.BadRequestException("Invalid JSON: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            throw new Exceptions.BadRequestException("Invalid JSON object: " + e.getMessage());
        }
    }
}
