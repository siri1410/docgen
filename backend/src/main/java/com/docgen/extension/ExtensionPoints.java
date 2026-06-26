package com.docgen.extension;

import java.util.Map;
import java.util.UUID;

/**
 * Marker interfaces reserving the platform's planned extension points. They are intentionally
 * unimplemented now — they document the seams where future capabilities plug in without
 * rewriting core logic. Provide a Spring bean implementing one of these to activate it.
 */
public final class ExtensionPoints {

    private ExtensionPoints() {}

    /** Render a submission/template into a document (PDF, DOCX, ...). */
    public interface DocumentGenerator {
        String format();
        byte[] generate(UUID submissionId, Map<String, Object> options);
    }

    /** Drive approval workflows over submissions. */
    public interface WorkflowEngine {
        void onSubmitted(UUID submissionId);
        String currentState(UUID submissionId);
    }

    /** Publish domain events to external systems. */
    public interface WebhookPublisher {
        void publish(String event, Map<String, Object> payload);
    }

    /** Capture and verify electronic signatures. */
    public interface ESignatureProvider {
        String requestSignature(UUID submissionId, String signerEmail);
    }

    /** Evaluate externalized business rules (conditional logic, eligibility, ...). */
    public interface RulesEngine {
        Map<String, Object> evaluate(String ruleSet, Map<String, Object> facts);
    }

    /** Map between the platform model and healthcare standards (e.g. FHIR). */
    public interface DataStandardMapper {
        String standard();
        Map<String, Object> toStandard(UUID submissionId);
    }
}
