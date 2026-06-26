package com.docgen.connector;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositories for connectors and field mappings.
 */
public final class ConnectorRepositories {

    private ConnectorRepositories() {}

    public interface ApiConnectorRepository extends JpaRepository<ApiConnector, UUID> {
        List<ApiConnector> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
    }

    public interface ApiFieldMappingRepository extends JpaRepository<ApiFieldMapping, UUID> {
        List<ApiFieldMapping> findByTemplateId(UUID templateId);
    }
}
