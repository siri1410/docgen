package com.docgen.connector;

import com.docgen.connector.ConnectorDtos.ConnectorRequest;
import com.docgen.connector.ConnectorDtos.ConnectorResponse;
import com.docgen.connector.ConnectorDtos.MappingResponse;
import com.docgen.connector.ConnectorDtos.MappingsRequest;
import com.docgen.connector.ConnectorDtos.TestRequest;
import com.docgen.connector.ConnectorDtos.TestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for API connectors and template field mappings.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Connectors", description = "External API connectors and field mappings")
public class ConnectorController {

    private final ConnectorService service;

    public ConnectorController(ConnectorService service) {
        this.service = service;
    }

    @Operation(summary = "Create an API connector")
    @PostMapping("/connectors")
    public ConnectorResponse create(@Valid @RequestBody ConnectorRequest req) {
        return service.create(req);
    }

    @Operation(summary = "List connectors")
    @GetMapping("/connectors")
    public List<ConnectorResponse> list() {
        return service.list();
    }

    @Operation(summary = "Test a connector by executing a live call")
    @PostMapping("/connectors/{id}/test")
    public TestResponse test(@PathVariable UUID id, @RequestBody(required = false) TestRequest req) {
        return service.test(id, req == null ? null : req.input());
    }

    @Operation(summary = "Replace a template's field mappings")
    @PostMapping("/templates/{templateId}/mappings")
    public List<MappingResponse> saveMappings(@PathVariable UUID templateId, @RequestBody MappingsRequest req) {
        return service.saveMappings(templateId, req.mappings());
    }

    @Operation(summary = "List a template's field mappings")
    @GetMapping("/templates/{templateId}/mappings")
    public List<MappingResponse> listMappings(@PathVariable UUID templateId) {
        return service.mappingResponses(templateId);
    }
}
