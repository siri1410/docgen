package com.docgen.audit;

import com.docgen.common.Json;
import com.docgen.user.CurrentTenant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records audit entries for security-relevant actions (template edits, submissions, connector changes).
 */
@Service
public class AuditService {

    private final AuditLogRepository repository;
    private final CurrentTenant currentTenant;

    public AuditService(AuditLogRepository repository, CurrentTenant currentTenant) {
        this.repository = repository;
        this.currentTenant = currentTenant;
    }

    @Transactional
    public void record(String action, String entityType, UUID entityId, Map<String, Object> detail) {
        AuditLog log = new AuditLog(
                currentTenant.currentActor(), action, entityType, entityId,
                detail == null ? null : Json.write(detail));
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> recent() {
        return repository.findTop100ByOrderByCreatedAtDesc();
    }
}
