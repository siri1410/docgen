package com.docgen.prefill;

import com.docgen.common.Exceptions.BadRequestException;
import com.docgen.common.Exceptions.RateLimitedException;
import com.docgen.connector.ApiConnector;
import com.docgen.connector.ApiConnectorClient;
import com.docgen.connector.ApiFieldMapping;
import com.docgen.connector.ConnectorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * The prefill engine. For a template it loads the configured {@link ApiFieldMapping}s, runs each
 * referenced connector once, then for every mapping extracts a value via JSONPath, applies the
 * named transform, falls back to a default and enforces the required flag.
 */
@Service
public class PrefillService {

    /** Per-field outcome returned to the client. */
    public record PrefillField(String target, Object value, String source, String connector, boolean fromFallback) {}

    /** Full prefill result. */
    public record PrefillResult(Map<String, Object> values, List<PrefillField> details, List<String> warnings) {}

    private static final Configuration JSONPATH_CONFIG = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .options(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL)
            .build();

    private final ConnectorService connectorService;
    private final ApiConnectorClient client;
    private final TransformFunctions transforms;
    private final PrefillRateLimiter rateLimiter;

    public PrefillService(ConnectorService connectorService, ApiConnectorClient client,
                         TransformFunctions transforms, PrefillRateLimiter rateLimiter) {
        this.connectorService = connectorService;
        this.client = client;
        this.transforms = transforms;
        this.rateLimiter = rateLimiter;
    }

    public PrefillResult prefill(UUID templateId, Map<String, Object> input) {
        if (!rateLimiter.tryAcquire("template:" + templateId)) {
            throw new RateLimitedException("Prefill rate limit exceeded for this template");
        }

        List<ApiFieldMapping> mappings = connectorService.mappingsForTemplate(templateId);
        if (mappings.isEmpty()) {
            return new PrefillResult(Map.of(), List.of(), List.of("No field mappings configured for this template"));
        }

        // Group mappings by connector and execute each connector exactly once.
        Map<UUID, List<ApiFieldMapping>> byConnector = new LinkedHashMap<>();
        for (ApiFieldMapping m : mappings) {
            byConnector.computeIfAbsent(m.getConnectorId(), k -> new ArrayList<>()).add(m);
        }

        Map<String, Object> values = new LinkedHashMap<>();
        List<PrefillField> details = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, Object> safeInput = input == null ? Map.of() : input;

        for (var entry : byConnector.entrySet()) {
            ApiConnector connector = connectorService.require(entry.getKey());
            JsonNode response;
            try {
                ApiConnectorClient.Result result =
                        client.execute(connector, connectorService.decryptSecret(connector), safeInput);
                if (!result.success()) {
                    warnings.add("Connector '" + connector.getName() + "' returned HTTP " + result.statusCode());
                }
                response = result.body();
            } catch (Exception e) {
                warnings.add("Connector '" + connector.getName() + "' failed: " + e.getMessage());
                response = null;
            }

            for (ApiFieldMapping m : entry.getValue()) {
                resolve(m, connector.getName(), response, values, details, warnings);
            }
        }

        return new PrefillResult(values, details, warnings);
    }

    private void resolve(ApiFieldMapping m, String connectorName, JsonNode response,
                         Map<String, Object> values, List<PrefillField> details, List<String> warnings) {
        Object extracted = null;
        boolean fromFallback = false;

        if (response != null && !response.isNull()) {
            try {
                JsonNode node = JsonPath.using(JSONPATH_CONFIG).parse(response).read(m.getSource(), JsonNode.class);
                extracted = toJavaValue(node);
            } catch (Exception e) {
                warnings.add("JSONPath '" + m.getSource() + "' failed: " + e.getMessage());
            }
        }

        if (extracted == null && m.getFallbackValue() != null) {
            extracted = m.getFallbackValue();
            fromFallback = true;
        }

        if (extracted instanceof String s) {
            extracted = transforms.apply(m.getTransform(), s);
        }

        if (extracted == null && m.isRequired()) {
            warnings.add("Required mapping for '" + m.getTarget() + "' produced no value");
        }

        if (extracted != null) {
            values.put(m.getTarget(), extracted);
            details.add(new PrefillField(m.getTarget(), extracted, m.getSource(), connectorName, fromFallback));
        }
    }

    private static Object toJavaValue(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isTextual()) return node.asText();
        if (node.isNumber()) return node.numberValue();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isArray()) {
            return com.docgen.common.Json.mapper().convertValue(node, List.class);
        }
        if (node.isObject()) {
            return new HashMap<>(com.docgen.common.Json.mapper().convertValue(node, Map.class));
        }
        return node.asText();
    }

    /** Validates that an input map is present for connectors that template variables into the request. */
    public void requireInput(Map<String, Object> input) {
        if (input == null || input.isEmpty()) {
            throw new BadRequestException("Prefill input is required");
        }
    }
}
