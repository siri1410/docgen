package com.docgen.document;

import com.docgen.extension.ExtensionPoints.DocumentGenerator;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.springframework.stereotype.Component;

/**
 * Renders a submission to an editable Word (.docx) document via Apache POI. Opens in Microsoft Word,
 * Google Docs, LibreOffice Writer and Pages. Registered automatically as a {@code DocumentGenerator}.
 */
@Component
public class DocxDocumentGenerator implements DocumentGenerator {

    private static final String ACCENT = "6366F1";
    private static final String MUTED = "64748B";

    private final DocumentModelBuilder builder;

    public DocxDocumentGenerator(DocumentModelBuilder builder) {
        this.builder = builder;
    }

    @Override
    public String format() {
        return "docx";
    }

    @Override
    public byte[] generate(UUID submissionId, Map<String, Object> options) {
        FormDocument m = builder.build(submissionId);
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            run(doc.createParagraph(), m.logo() + "  " + m.department().toUpperCase(), 11, true, ACCENT);
            run(doc.createParagraph(), m.title(), 22, true, "000000");
            if (m.subtitle() != null && !m.subtitle().isBlank()) {
                run(doc.createParagraph(), m.subtitle(), 9, false, MUTED);
            }
            XWPFParagraph meta = doc.createParagraph();
            run(meta, "Form: " + m.formId() + "    ·    Version " + m.version()
                    + "    ·    Submitted " + m.submittedAt(), 9, false, MUTED);
            bottomBorder(meta);

            for (FormDocument.Block b : m.blocks()) {
                if (b.section()) {
                    XWPFParagraph s = doc.createParagraph();
                    s.setSpacingBefore(220);
                    run(s, b.label(), 13, true, "000000");
                    bottomBorder(s);
                } else {
                    XWPFParagraph p = doc.createParagraph();
                    p.setSpacingAfter(60);
                    run(p, b.label() + (b.sensitive() ? " (sensitive)" : "") + ":  ", 10, true, MUTED);
                    run(p, b.value(), 11, false, "000000");
                }
            }

            XWPFParagraph sig = doc.createParagraph();
            sig.setSpacingBefore(600);
            run(sig, "_____________________________          _____________________", 11, false, "000000");
            XWPFParagraph cap = doc.createParagraph();
            run(cap, "Signature                                                  Date", 9, false, MUTED);

            doc.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("DOCX generation failed: " + e.getMessage(), e);
        }
    }

    private static void run(XWPFParagraph p, String text, int pt, boolean bold, String colorHex) {
        if (p.getAlignment() == null) {
            p.setAlignment(ParagraphAlignment.LEFT);
        }
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setBold(bold);
        r.setFontSize(pt);
        r.setColor(colorHex);
        r.setFontFamily("Helvetica");
    }

    private static void bottomBorder(XWPFParagraph p) {
        var border = p.getCTP().addNewPPr().addNewPBdr().addNewBottom();
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(6));
        border.setSpace(BigInteger.valueOf(1));
        border.setColor("E2E8F0");
    }
}
