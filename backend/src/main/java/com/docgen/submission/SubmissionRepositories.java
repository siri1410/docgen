package com.docgen.submission;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for submissions.
 */
public interface SubmissionRepositories extends JpaRepository<FormSubmission, UUID> {
    List<FormSubmission> findByTemplateIdOrderByCreatedAtDesc(UUID templateId);
}
