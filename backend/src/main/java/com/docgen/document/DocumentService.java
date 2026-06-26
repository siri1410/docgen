package com.docgen.document;

import com.docgen.common.Exceptions.BadRequestException;
import com.docgen.extension.ExtensionPoints.DocumentGenerator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Routes a document request to the right {@link DocumentGenerator} bean by format. Any bean
 * implementing the extension point is picked up automatically, so adding a format (e.g. ODT, XLSX)
 * is one new bean — no change here or in the controller.
 */
@Service
public class DocumentService {

    /** Display metadata for a supported output format. */
    public record FormatInfo(String format, String label, String contentType, String extension) {}

    private static final Map<String, FormatInfo> META = Map.of(
            "pdf", new FormatInfo("pdf", "PDF", "application/pdf", "pdf"),
            "docx", new FormatInfo("docx", "Word (.docx)",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
            "rtf", new FormatInfo("rtf", "Rich Text (.rtf)", "application/rtf", "rtf"),
            "html", new FormatInfo("html", "Web Page (.html)", "text/html", "html"));

    private final Map<String, DocumentGenerator> byFormat = new LinkedHashMap<>();

    public DocumentService(List<DocumentGenerator> generators) {
        for (DocumentGenerator g : generators) {
            byFormat.put(g.format().toLowerCase(), g);
        }
    }

    /** Formats that actually have a registered generator, with their display metadata. */
    public List<FormatInfo> supportedFormats() {
        return byFormat.keySet().stream()
                .map(f -> META.getOrDefault(f, new FormatInfo(f, f.toUpperCase(), "application/octet-stream", f)))
                .toList();
    }

    public FormatInfo info(String format) {
        String key = format == null ? "" : format.toLowerCase();
        if (!byFormat.containsKey(key)) {
            throw new BadRequestException("Unsupported document format: " + format
                    + ". Supported: " + byFormat.keySet());
        }
        return META.getOrDefault(key, new FormatInfo(key, key.toUpperCase(), "application/octet-stream", key));
    }

    public byte[] generate(UUID submissionId, String format, Map<String, Object> options) {
        DocumentGenerator g = byFormat.get(format == null ? "" : format.toLowerCase());
        if (g == null) {
            throw new BadRequestException("Unsupported document format: " + format
                    + ". Supported: " + byFormat.keySet());
        }
        return g.generate(submissionId, options == null ? Map.of() : options);
    }
}
