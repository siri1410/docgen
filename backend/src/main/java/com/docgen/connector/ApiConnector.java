package com.docgen.connector;

import com.docgen.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A configurable, plugin-style connection to an external REST API used for prefill.
 *
 * <p>Secrets ({@code secretJson}) are encrypted at rest via {@code CryptoService} in the service layer.
 * Headers, query params and a request-body template are stored as extensible JSONB.
 */
@Entity
@Table(name = "api_connector")
public class ApiConnector extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false)
    private String name;

    @Column(name = "base_url", nullable = false)
    private String baseUrl;

    @Column(name = "http_method", nullable = false)
    private String httpMethod = "GET";

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false)
    private AuthType authType = AuthType.NONE;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "headers_json", columnDefinition = "jsonb")
    private String headersJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "query_params_json", columnDefinition = "jsonb")
    private String queryParamsJson;

    @Column(name = "request_body_template", columnDefinition = "text")
    private String requestBodyTemplate;

    /** Encrypted JSON blob of credentials (e.g. {"token":"..."} / {"username":"...","password":"..."}). */
    @Column(name = "secret_json", columnDefinition = "text")
    private String secretJson;

    protected ApiConnector() {
    }

    public ApiConnector(UUID organizationId, String name, String baseUrl) {
        this.organizationId = organizationId;
        this.name = name;
        this.baseUrl = baseUrl;
    }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public AuthType getAuthType() { return authType; }
    public void setAuthType(AuthType authType) { this.authType = authType; }

    public String getHeadersJson() { return headersJson; }
    public void setHeadersJson(String headersJson) { this.headersJson = headersJson; }

    public String getQueryParamsJson() { return queryParamsJson; }
    public void setQueryParamsJson(String queryParamsJson) { this.queryParamsJson = queryParamsJson; }

    public String getRequestBodyTemplate() { return requestBodyTemplate; }
    public void setRequestBodyTemplate(String requestBodyTemplate) { this.requestBodyTemplate = requestBodyTemplate; }

    public String getSecretJson() { return secretJson; }
    public void setSecretJson(String secretJson) { this.secretJson = secretJson; }
}
