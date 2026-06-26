package com.docgen.prefill;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.docgen.common.Json;
import com.docgen.connector.ApiConnector;
import com.docgen.connector.ApiConnectorClient;
import com.docgen.connector.ApiFieldMapping;
import com.docgen.connector.ConnectorService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrefillServiceTest {

    @Mock ConnectorService connectorService;
    @Mock ApiConnectorClient client;
    @Mock PrefillRateLimiter rateLimiter;

    private PrefillService service;
    private final UUID templateId = UUID.randomUUID();
    private final UUID connectorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new PrefillService(connectorService, client, new TransformFunctions(), rateLimiter);
        when(rateLimiter.tryAcquire(anyString())).thenReturn(true);
    }

    private ApiFieldMapping mapping(String source, String target, String transform, String fallback, boolean required) {
        ApiFieldMapping m = new ApiFieldMapping(templateId, connectorId, source, target);
        m.setTransform(transform);
        m.setFallbackValue(fallback);
        m.setRequired(required);
        return m;
    }

    @Test
    void extractsTransformsAndFallsBack() {
        when(connectorService.mappingsForTemplate(templateId)).thenReturn(List.of(
                mapping("$.member.firstName", "firstName", "capitalize", null, true),
                mapping("$.member.address.state", "state", "upper", null, false),
                mapping("$.member.missing", "mpi", null, "MPI-DEFAULT", false)));

        ApiConnector connector = new ApiConnector(UUID.randomUUID(), "Member API", "https://x");
        when(connectorService.require(connectorId)).thenReturn(connector);
        when(connectorService.decryptSecret(any())).thenReturn(Map.of());

        var body = Json.read("{\"member\":{\"firstName\":\"john\",\"address\":{\"state\":\"nc\"}}}");
        when(client.execute(any(), any(), any()))
                .thenReturn(new ApiConnectorClient.Result(true, 200, body, null));

        PrefillService.PrefillResult result = service.prefill(templateId, Map.of("memberId", "M1"));

        assertThat(result.values())
                .containsEntry("firstName", "John")   // capitalize transform
                .containsEntry("state", "NC")          // upper transform
                .containsEntry("mpi", "MPI-DEFAULT");  // fallback used
    }

    @Test
    void usesFallbacksWhenConnectorFails() {
        when(connectorService.mappingsForTemplate(templateId)).thenReturn(List.of(
                mapping("$.member.firstName", "firstName", "capitalize", "Jane", true)));
        when(connectorService.require(connectorId))
                .thenReturn(new ApiConnector(UUID.randomUUID(), "API", "https://x"));
        when(connectorService.decryptSecret(any())).thenReturn(Map.of());
        when(client.execute(any(), any(), any())).thenThrow(new RuntimeException("boom"));

        PrefillService.PrefillResult result = service.prefill(templateId, Map.of());

        assertThat(result.values()).containsEntry("firstName", "Jane");
        assertThat(result.warnings()).isNotEmpty();
    }

    @Test
    void emptyWhenNoMappings() {
        when(connectorService.mappingsForTemplate(templateId)).thenReturn(List.of());
        PrefillService.PrefillResult result = service.prefill(templateId, Map.of());
        assertThat(result.values()).isEmpty();
        assertThat(result.warnings()).isNotEmpty();
    }
}
