package com.docgen.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only access to the most recent audit entries (for the admin settings screen).
 */
@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit", description = "Audit trail")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @Operation(summary = "List the 100 most recent audit entries")
    @GetMapping
    public List<AuditLog> recent() {
        return auditService.recent();
    }
}
