package com.docgen.document;

import com.docgen.document.DocumentService.FormatInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Server-side document generation. Produces a downloadable PDF / Word / RTF rendering of a submission
 * via the {@code DocumentGenerator} extension beans — no browser print dialog required.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Documents", description = "Generate PDF / Word / RTF documents from submissions")
public class DocumentController {

    private final DocumentService documents;

    public DocumentController(DocumentService documents) {
        this.documents = documents;
    }

    @Operation(summary = "List the document output formats this server can generate")
    @GetMapping("/documents/formats")
    public List<FormatInfo> formats() {
        return documents.supportedFormats();
    }

    @Operation(summary = "Generate and download a submission as a document (format=pdf|docx|rtf)")
    @GetMapping("/submissions/{id}/document")
    public ResponseEntity<byte[]> document(@PathVariable UUID id,
                                           @RequestParam(defaultValue = "pdf") String format) {
        FormatInfo info = documents.info(format);
        byte[] body = documents.generate(id, format, Map.of());
        String filename = "form-" + id.toString().substring(0, 8) + "." + info.extension();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(info.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(body);
    }
}
