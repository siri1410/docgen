package com.docgen.connector;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Request/response DTOs for connectors and mappings.
 */
public final class ConnectorDtos {

    private ConnectorDtos() {}

    public record ConnectorRequest(
            @NotBlank String name,
            @NotBlank String baseUrl,
            String httpMethod,
            AuthType authType,
            JsonNode headers,
            JsonNode queryParams,
            String requestBodyTemplate,
            /** Plain credentials; encrypted at rest by the service before persisting. */
            JsonNode secret) {}

    public record ConnectorResponse(
            UUID id,
            String name,
            String baseUrl,
            String httpMethod,
            AuthType authType,
            JsonNode headers,
            JsonNode queryParams,
            String requestBodyTemplate,
            boolean hasSecret,
            Instant createdAt,
            Instant updatedAt) {}

    public record TestRequest(Map<String, Object> input) {}

    public record TestResponse(boolean success, int statusCode, JsonNode body, String error) {}

    public record MappingRequest(
            UUID connectorId,
            @NotBlank String source,
            @NotBlank String target,
            String transform,
            String fallbackValue,
            boolean required) {}

    public record MappingResponse(
            UUID id,
            UUID templateId,
            UUID connectorId,
            String source,
            String target,
            String transform,
            String fallbackValue,
            boolean required) {}

    public record MappingsRequest(List<MappingRequest> mappings) {}
}
