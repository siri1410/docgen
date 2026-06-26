package com.docgen.connector;

import static org.assertj.core.api.Assertions.assertThat;

import com.docgen.common.Json;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class ApiConnectorClientTest {

    private MockWebServer server;
    private ApiConnectorClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        client = new ApiConnectorClient(RestClient.builder(), List.of(
                new ApiConnectorClient.NoAuth(),
                new ApiConnectorClient.BearerAuth(),
                new ApiConnectorClient.BasicAuth(),
                new ApiConnectorClient.ApiKeyAuth(),
                new ApiConnectorClient.OAuth2Auth()));
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void executesTemplatedGetAndParsesJson() throws Exception {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"member\":{\"firstName\":\"john\"}}"));

        // Build the base URL with a raw (un-encoded) placeholder so templating runs before URI parsing.
        String base = server.url("/").toString() + "members/{{memberId}}";
        ApiConnector connector = new ApiConnector(UUID.randomUUID(), "Member API", base);
        connector.setHttpMethod("GET");
        connector.setAuthType(AuthType.BEARER);

        ApiConnectorClient.Result result = client.execute(
                connector, Map.of("token", "abc123"), Map.of("memberId", "M-42"));

        assertThat(result.success()).isTrue();
        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.body().at("/member/firstName").asText()).isEqualTo("john");

        RecordedRequest recorded = server.takeRequest();
        assertThat(recorded.getPath()).isEqualTo("/members/M-42");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer abc123");
    }

    @Test
    void appliesApiKeyHeader() throws Exception {
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{}"));
        ApiConnector connector = new ApiConnector(UUID.randomUUID(), "K", server.url("/x").toString());
        connector.setAuthType(AuthType.API_KEY);

        client.execute(connector, Json.readMap("{\"headerName\":\"X-Key\",\"apiKey\":\"secret\"}"), Map.of());

        assertThat(server.takeRequest().getHeader("X-Key")).isEqualTo("secret");
    }
}
