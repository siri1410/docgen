package com.docgen.document;

import com.docgen.extension.ExtensionPoints.DocumentGenerator;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Renders a submission to Rich Text Format (.rtf) with no external library. RTF is the most widely
 * compatible word-processing format — it opens in Word, Pages, LibreOffice, WordPad and Google Docs —
 * which makes it a dependency-free "other office tools" fallback. Registered as a {@code DocumentGenerator}.
 */
@Component
public class RtfDocumentGenerator implements DocumentGenerator {

    private final DocumentModelBuilder builder;

    public RtfDocumentGenerator(DocumentModelBuilder builder) {
        this.builder = builder;
    }

    @Override
    public String format() {
        return "rtf";
    }

    @Override
    public byte[] generate(UUID submissionId, Map<String, Object> options) {
        FormDocument m = builder.build(submissionId);
        StringBuilder sb = new StringBuilder();
        // \fs is half-points; colors: 1=accent, 2=muted, 3=black.
        sb.append("{\\rtf1\\ansi\\ansicpg1252\\deff0")
          .append("{\\fonttbl{\\f0\\fswiss Helvetica;}}")
          .append("{\\colortbl;\\red99\\green102\\blue241;\\red100\\green116\\blue139;\\red15\\green23\\blue42;}")
          .append("\\f0\\fs22 ");

        line(sb, esc(m.department().toUpperCase()), 20, true, 1);
        line(sb, esc(m.title()), 36, true, 3);
        if (m.subtitle() != null && !m.subtitle().isBlank()) {
            line(sb, esc(m.subtitle()), 18, false, 2);
        }
        line(sb, esc("Form: " + m.formId() + "    ·    Version " + m.version()
                + "    ·    Submitted " + m.submittedAt()), 18, false, 2);
        sb.append("\\par ");

        for (FormDocument.Block b : m.blocks()) {
            if (b.section()) {
                sb.append("\\par ");
                line(sb, esc(b.label()), 26, true, 3);
            } else {
                String label = esc(b.label() + (b.sensitive() ? " (sensitive)" : "") + ":  ");
                sb.append("{\\cf2\\b\\fs20 ").append(label).append("\\b0}")
                  .append("{\\cf3\\fs22 ").append(esc(b.value())).append("}\\line ");
            }
        }

        sb.append("\\par\\par ")
          .append(esc("_____________________________          _____________________")).append("\\line ");
        line(sb, esc("Signature                                                  Date"), 18, false, 2);

        sb.append("}");
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private static void line(StringBuilder sb, String text, int halfPoints, boolean bold, int color) {
        sb.append("{\\cf").append(color).append(bold ? "\\b" : "").append("\\fs").append(halfPoints).append(' ')
          .append(text).append(bold ? "\\b0" : "").append("}\\line ");
    }

    /** Escape RTF control chars and encode any non-ASCII as \\uN unicode escapes. */
    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '{' || c == '}') {
                b.append('\\').append(c);
            } else if (c < 128) {
                b.append(c);
            } else {
                // RTF unicode: \\uN with a fallback char so non-Unicode readers degrade gracefully.
                b.append("\\u").append((int) c).append("?");
            }
        }
        return b.toString();
    }
}
