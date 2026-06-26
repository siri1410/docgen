package com.docgen.template;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositories for the template aggregate.
 */
public final class TemplateRepositories {

    private TemplateRepositories() {}

    public interface FormTemplateRepository extends JpaRepository<FormTemplate, UUID> {
        List<FormTemplate> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
    }

    public interface FormFieldRepository extends JpaRepository<FormField, UUID> {
        List<FormField> findByTemplateIdOrderByOrderIndexAsc(UUID templateId);
    }

    public interface FormTemplateVersionRepository extends JpaRepository<FormTemplateVersion, UUID> {
        List<FormTemplateVersion> findByTemplateIdOrderByVersionNumberDesc(UUID templateId);
    }
}
