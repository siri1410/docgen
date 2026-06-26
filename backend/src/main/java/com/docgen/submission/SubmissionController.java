package com.docgen.submission;

import com.docgen.submission.SubmissionDtos.SubmissionResponse;
import com.docgen.submission.SubmissionDtos.SubmissionSummary;
import com.docgen.submission.SubmissionDtos.SubmitRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for form submissions.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Submissions", description = "Submit and read form data")
public class SubmissionController {

    private final SubmissionService service;

    public SubmissionController(SubmissionService service) {
        this.service = service;
    }

    @Operation(summary = "Submit a form (server-side validation + encryption of sensitive values)")
    @PostMapping("/forms/{templateId}/submit")
    public SubmissionResponse submit(@PathVariable UUID templateId, @RequestBody SubmitRequest req) {
        return service.submit(templateId, req.values());
    }

    @Operation(summary = "List submissions for a template")
    @GetMapping("/forms/{templateId}/submissions")
    public List<SubmissionSummary> list(@PathVariable UUID templateId) {
        return service.listForTemplate(templateId);
    }

    @Operation(summary = "Get a submission (sensitive values masked unless authorized)")
    @GetMapping("/submissions/{id}")
    public SubmissionResponse get(@PathVariable UUID id) {
        return service.get(id);
    }
}
