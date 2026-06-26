package com.docgen.prefill;

import com.docgen.prefill.PrefillService.PrefillResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for the prefill engine.
 */
@RestController
@RequestMapping("/api/forms")
@Tag(name = "Prefill", description = "Auto-populate form fields from external APIs")
public class PrefillController {

    private final PrefillService prefillService;

    public PrefillController(PrefillService prefillService) {
        this.prefillService = prefillService;
    }

    public record PrefillRequest(Map<String, Object> input) {}

    @Operation(summary = "Prefill a form's fields by running its configured connectors + mappings")
    @PostMapping("/{templateId}/prefill")
    public PrefillResult prefill(@PathVariable UUID templateId, @RequestBody(required = false) PrefillRequest req) {
        return prefillService.prefill(templateId, req == null ? Map.of() : req.input());
    }
}
