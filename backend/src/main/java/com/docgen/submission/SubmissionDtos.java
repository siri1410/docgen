package com.docgen.submission;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Request/response DTOs for submissions.
 */
public final class SubmissionDtos {

    private SubmissionDtos() {}

    /** Submitted form values keyed by field key. */
    public record SubmitRequest(Map<String, Object> values) {}

    public record SubmissionResponse(
            UUID id,
            UUID templateId,
            int templateVersion,
            String submittedBy,
            /** Values, with sensitive fields masked unless the caller is authorized to unmask. */
            Map<String, Object> values,
            Instant createdAt) {}

    public record SubmissionSummary(
            UUID id,
            UUID templateId,
            String submittedBy,
            int fieldCount,
            Instant createdAt) {}
}
