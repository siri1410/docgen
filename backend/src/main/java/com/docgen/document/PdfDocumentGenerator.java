package com.docgen.document;

import com.docgen.extension.ExtensionPoints.DocumentGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Renders a submission to a print-ready PDF via OpenPDF — the server-side equivalent of the
 * browser print dialog. Registered automatically as a {@code DocumentGenerator} extension bean.
 */
@Component
public class PdfDocumentGenerator implements DocumentGenerator {

    private static final Color ACCENT = new Color(0x63, 0x66, 0xF1);
    private static final Color MUTED = new Color(0x64, 0x74, 0x8B);
    private static final Color LINE = new Color(0xCB, 0xD5, 0xE1);

    private final DocumentModelBuilder builder;

    public PdfDocumentGenerator(DocumentModelBuilder builder) {
        this.builder = builder;
    }

    @Override
    public String format() {
        return "pdf";
    }

    @Override
    public byte[] generate(UUID submissionId, Map<String, Object> options) {
        FormDocument m = builder.build(submissionId);
        Document doc = new Document(PageSize.A4, 48, 48, 54, 54);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(paragraph(m.department().toUpperCase(), font(10, Font.BOLD, ACCENT)));
            doc.add(paragraph(m.title(), font(18, Font.BOLD, Color.BLACK)));
            if (m.subtitle() != null && !m.subtitle().isBlank()) {
                doc.add(paragraph(m.subtitle(), font(9, Font.NORMAL, MUTED)));
            }
            doc.add(paragraph("Form: " + m.formId() + "    ·    Version " + m.version()
                    + "    ·    Submitted " + m.submittedAt(), font(9, Font.NORMAL, MUTED)));
            LineSeparator rule = new LineSeparator(2f, 100f, ACCENT, Element.ALIGN_CENTER, -4f);
            doc.add(new Paragraph(new com.lowagie.text.Chunk(rule)));

            for (FormDocument.Block b : m.blocks()) {
                if (b.section()) {
                    Paragraph s = paragraph(b.label(), font(12, Font.BOLD, Color.BLACK));
                    s.setSpacingBefore(14f);
                    s.setSpacingAfter(4f);
                    doc.add(s);
                } else {
                    doc.add(fieldRow(b));
                }
            }

            doc.add(signature());
            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("PDF generation failed: " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    private PdfPTable fieldRow(FormDocument.Block b) {
        PdfPTable t = new PdfPTable(new float[] {34f, 66f});
        t.setWidthPercentage(100);
        t.setSpacingBefore(3f);

        String label = b.label() + (b.sensitive() ? "  (sensitive)" : "");
        PdfPCell lc = new PdfPCell(new Phrase(label, font(10, Font.BOLD, MUTED)));
        lc.setBorder(Rectangle.NO_BORDER);
        lc.setPaddingBottom(5f);

        PdfPCell vc = new PdfPCell(new Phrase(b.value(), font(11, Font.NORMAL, Color.BLACK)));
        vc.setBorder(Rectangle.BOTTOM);
        vc.setBorderColor(LINE);
        vc.setPaddingBottom(5f);

        t.addCell(lc);
        t.addCell(vc);
        return t;
    }

    private PdfPTable signature() {
        PdfPTable t = new PdfPTable(new float[] {60f, 40f});
        t.setWidthPercentage(100);
        t.setSpacingBefore(36f);
        t.addCell(sigCell("Signature"));
        t.addCell(sigCell("Date"));
        return t;
    }

    private PdfPCell sigCell(String caption) {
        PdfPCell c = new PdfPCell(new Phrase("\n" + caption, font(9, Font.NORMAL, MUTED)));
        c.setBorder(Rectangle.TOP);
        c.setBorderColor(Color.BLACK);
        c.setPaddingTop(2f);
        c.setPaddingRight(24f);
        return c;
    }

    private static Font font(float size, int style, Color color) {
        return new Font(Font.HELVETICA, size, style, color);
    }

    private static Paragraph paragraph(String text, Font f) {
        Paragraph p = new Paragraph(text, f);
        p.setSpacingAfter(2f);
        return p;
    }
}
