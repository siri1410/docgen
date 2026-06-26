package com.docgen.connector;

import com.docgen.audit.AuditService;
import com.docgen.common.Exceptions.NotFoundException;
import com.docgen.common.Json;
import com.docgen.connector.ConnectorDtos.ConnectorRequest;
import com.docgen.connector.ConnectorDtos.ConnectorResponse;
import com.docgen.connector.ConnectorDtos.MappingRequest;
import com.docgen.connector.ConnectorDtos.MappingResponse;
import com.docgen.connector.ConnectorRepositories.ApiConnectorRepository;
import com.docgen.connector.ConnectorRepositories.ApiFieldMappingRepository;
import com.docgen.security.CryptoService;
import com.docgen.user.CurrentTenant;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages API connectors and their field mappings, encrypting connector secrets at rest and
 * executing test calls. Used by the prefill engine to resolve and run connectors.
 */
@Service
public class ConnectorService {

    private final ApiConnectorRepository connectors;
    private final ApiFieldMappingRepository mappings;
    private final ApiConnectorClient client;
    private final CryptoService crypto;
    private final CurrentTenant currentTenant;
    private final AuditService audit;

    public ConnectorService(ApiConnectorRepository connectors, ApiFieldMappingRepository mappings,
                           ApiConnectorClient client, CryptoService crypto,
                           CurrentTenant currentTenant, AuditService audit) {
        this.connectors = connectors;
        this.mappings = mappings;
        this.client = client;
        this.crypto = crypto;
        this.currentTenant = currentTenant;
        this.audit = audit;
    }

    @Transactional
    public ConnectorResponse create(ConnectorRequest req) {
        ApiConnector c = new ApiConnector(currentTenant.organizationId(), req.name(), req.baseUrl());
        apply(c, req);
        connectors.save(c);
        audit.record("CONNECTOR_CREATE", "ApiConnector", c.getId(), Map.of("name", c.getName()));
        return toResponse(c);
    }

    @Transactional(readOnly = true)
    public List<ConnectorResponse> list() {
        return connectors.findByOrganizationIdOrderByCreatedAtDesc(currentTenant.organizationId())
                .stream().map(ConnectorService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApiConnector require(UUID id) {
        return connectors.findById(id).orElseThrow(() -> NotFoundException.of("ApiConnector", id));
    }

    /** Decrypt a connector's stored secret into a map for execution. */
    public Map<String, Object> decryptSecret(ApiConnector c) {
        if (c.getSecretJson() == null) {
            return Map.of();
        }
        return Json.readMap(crypto.decrypt(c.getSecretJson()));
    }

    @Transactional(readOnly = true)
    public ConnectorDtos.TestResponse test(UUID id, Map<String, Object> input) {
        ApiConnector c = require(id);
        ApiConnectorClient.Result r = client.execute(c, decryptSecret(c), input == null ? Map.of() : input);
        return new ConnectorDtos.TestResponse(r.success(), r.statusCode(), r.body(), r.error());
    }

    @Transactional
    public List<MappingResponse> saveMappings(UUID templateId, List<MappingRequest> requests) {
        mappings.deleteAll(mappings.findByTemplateId(templateId));
        List<ApiFieldMapping> saved = requests.stream().map(m -> {
            ApiFieldMapping mapping = new ApiFieldMapping(templateId, m.connectorId(), m.source(), m.target());
            mapping.setTransform(m.transform());
            mapping.setFallbackValue(m.fallbackValue());
            mapping.setRequired(m.required());
            return mappings.save(mapping);
        }).toList();
        audit.record("MAPPINGS_SAVE", "FormTemplate", templateId, Map.of("count", saved.size()));
        return saved.stream().map(ConnectorService::toMappingResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ApiFieldMapping> mappingsForTemplate(UUID templateId) {
        return mappings.findByTemplateId(templateId);
    }

    @Transactional(readOnly = true)
    public List<MappingResponse> mappingResponses(UUID templateId) {
        return mappingsForTemplate(templateId).stream().map(ConnectorService::toMappingResponse).toList();
    }

    private void apply(ApiConnector c, ConnectorRequest req) {
        c.setName(req.name());
        c.setBaseUrl(req.baseUrl());
        if (req.httpMethod() != null) c.setHttpMethod(req.httpMethod());
        if (req.authType() != null) c.setAuthType(req.authType());
        c.setHeadersJson(toJson(req.headers()));
        c.setQueryParamsJson(toJson(req.queryParams()));
        c.setRequestBodyTemplate(req.requestBodyTemplate());
        if (req.secret() != null && !req.secret().isNull()) {
            c.setSecretJson(crypto.encrypt(Json.write(req.secret())));
        }
    }

    private static String toJson(JsonNode node) {
        return node == null || node.isNull() ? null : Json.write(node);
    }

    private static ConnectorResponse toResponse(ApiConnector c) {
        return new ConnectorResponse(
                c.getId(), c.getName(), c.getBaseUrl(), c.getHttpMethod(), c.getAuthType(),
                c.getHeadersJson() == null ? null : Json.read(c.getHeadersJson()),
                c.getQueryParamsJson() == null ? null : Json.read(c.getQueryParamsJson()),
                c.getRequestBodyTemplate(),
                c.getSecretJson() != null,
                c.getCreatedAt(), c.getUpdatedAt());
    }

    private static MappingResponse toMappingResponse(ApiFieldMapping m) {
        return new MappingResponse(m.getId(), m.getTemplateId(), m.getConnectorId(),
                m.getSource(), m.getTarget(), m.getTransform(), m.getFallbackValue(), m.isRequired());
    }
}
