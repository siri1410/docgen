package com.docgen.connector;

import com.docgen.common.Exceptions.ConnectorException;
import com.docgen.common.Json;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Executes an {@link ApiConnector} call. Auth handling is pluggable: one {@link AuthStrategy}
 * bean per {@link AuthType}, looked up at call time — adding a new auth scheme means adding a
 * strategy, with no change to the executor.
 *
 * <p>Query params, the request body template and URL all support {@code {{var}}} placeholders
 * resolved from the caller-supplied input map.
 */
@Component
public class ApiConnectorClient {

    /** Result of an executed connector call. */
    public record Result(boolean success, int statusCode, JsonNode body, String error) {}

    /** Pluggable auth scheme. */
    public interface AuthStrategy {
        AuthType type();
        void apply(HttpHeaders headers, MultiValueMap<String, String> query, Map<String, Object> secret);
    }

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([\\w.]+)\\s*}}");

    private final RestClient restClient;
    private final Map<AuthType, AuthStrategy> authStrategies;

    public ApiConnectorClient(RestClient.Builder builder, List<AuthStrategy> strategies) {
        this.restClient = builder.build();
        this.authStrategies = strategies.stream()
                .collect(Collectors.toMap(AuthStrategy::type, Function.identity(), (a, b) -> a));
    }

    public Result execute(ApiConnector connector, Map<String, Object> decryptedSecret, Map<String, Object> input) {
        try {
            HttpMethod method = HttpMethod.valueOf(connector.getHttpMethod().toUpperCase());

            UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(render(connector.getBaseUrl(), input));
            MultiValueMap<String, String> query = new LinkedMultiValueMap<>();
            Json.readMap(connector.getQueryParamsJson()).forEach((k, v) ->
                    query.add(k, render(String.valueOf(v), input)));

            HttpHeaders headers = new HttpHeaders();
            Json.readMap(connector.getHeadersJson()).forEach((k, v) ->
                    headers.add(k, render(String.valueOf(v), input)));

            AuthStrategy auth = authStrategies.getOrDefault(connector.getAuthType(),
                    authStrategies.get(AuthType.NONE));
            if (auth != null) {
                auth.apply(headers, query, decryptedSecret == null ? Map.of() : decryptedSecret);
            }
            query.forEach((k, vals) -> vals.forEach(val -> uri.queryParam(k, val)));

            String body = connector.getRequestBodyTemplate() == null ? null
                    : render(connector.getRequestBodyTemplate(), input);

            RestClient.RequestBodySpec spec = restClient.method(method)
                    .uri(uri.build(true).toUri())
                    .headers(h -> h.addAll(headers));
            if (body != null && !body.isBlank()) {
                spec.contentType(MediaType.APPLICATION_JSON).body(body);
            }

            var response = spec.retrieve().toEntity(String.class);
            int status = response.getStatusCode().value();
            JsonNode json = Json.read(response.getBody());
            return new Result(response.getStatusCode().is2xxSuccessful(), status, json, null);
        } catch (Exception e) {
            throw new ConnectorException("Connector call failed: " + e.getMessage(), e);
        }
    }

    private static String render(String template, Map<String, Object> input) {
        if (template == null || input == null || input.isEmpty()) {
            return template;
        }
        Matcher m = PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            Object value = input.getOrDefault(m.group(1), "");
            m.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(value)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    // ---- Auth strategies (one bean per AuthType) ----

    @Component
    static class NoAuth implements AuthStrategy {
        @Override public AuthType type() { return AuthType.NONE; }
        @Override public void apply(HttpHeaders h, MultiValueMap<String, String> q, Map<String, Object> s) { }
    }

    @Component
    static class BearerAuth implements AuthStrategy {
        @Override public AuthType type() { return AuthType.BEARER; }
        @Override public void apply(HttpHeaders h, MultiValueMap<String, String> q, Map<String, Object> s) {
            Object token = s.get("token");
            if (token != null) {
                h.setBearerAuth(String.valueOf(token));
            }
        }
    }

    @Component
    static class BasicAuth implements AuthStrategy {
        @Override public AuthType type() { return AuthType.BASIC; }
        @Override public void apply(HttpHeaders h, MultiValueMap<String, String> q, Map<String, Object> s) {
            Object u = s.get("username");
            Object p = s.get("password");
            if (u != null && p != null) {
                String creds = Base64.getEncoder().encodeToString(
                        (u + ":" + p).getBytes(StandardCharsets.UTF_8));
                h.add(HttpHeaders.AUTHORIZATION, "Basic " + creds);
            }
        }
    }

    @Component
    static class ApiKeyAuth implements AuthStrategy {
        @Override public AuthType type() { return AuthType.API_KEY; }
        @Override public void apply(HttpHeaders h, MultiValueMap<String, String> q, Map<String, Object> s) {
            String headerName = String.valueOf(s.getOrDefault("headerName", "X-API-Key"));
            Object key = s.get("apiKey");
            if (key != null) {
                if (Boolean.parseBoolean(String.valueOf(s.getOrDefault("inQuery", "false")))) {
                    q.add(String.valueOf(s.getOrDefault("queryName", "api_key")), String.valueOf(key));
                } else {
                    h.add(headerName, String.valueOf(key));
                }
            }
        }
    }

    @Component
    static class OAuth2Auth implements AuthStrategy {
        @Override public AuthType type() { return AuthType.OAUTH2; }
        @Override public void apply(HttpHeaders h, MultiValueMap<String, String> q, Map<String, Object> s) {
            // Pre-obtained access token flow. A full client-credentials grant is an extension point.
            Object token = s.getOrDefault("accessToken", s.get("token"));
            if (token != null) {
                h.setBearerAuth(String.valueOf(token));
            }
        }
    }

    /** Exposed for callers that need to merge defaults. */
    static Map<String, Object> mutable(Map<String, Object> in) {
        return in == null ? new HashMap<>() : new HashMap<>(in);
    }
}
