package com.locus.service.impl;

import com.locus.model.ValuationReport;
import com.locus.service.ReportPdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

public class ReportPdfServiceImpl implements ReportPdfService {

    private static final float MARGIN_LEFT = 56f;
    private static final float START_Y = 760f;
    private static final float LINE_GAP = 18f;

    @Override
    public Path generatePdf(ValuationReport report) throws IOException {
        Path outputDirectory = Paths.get("generated-reports");
        Files.createDirectories(outputDirectory);

        String filename = "valuation-report-" + report.getReportId() + ".pdf";
        Path outputFile = outputDirectory.resolve(filename).toAbsolutePath().normalize();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = START_Y;
                drawHeader(content, page);
                y = writeLine(content, "LOCUS Analytics - Valuation Report", MARGIN_LEFT, y, PDType1Font.HELVETICA_BOLD, 18);
                y -= 6;
                y = writeLine(content, "Generated: " + LocalDate.now(), MARGIN_LEFT, y, PDType1Font.HELVETICA, 11);
                y = writeLine(content, "Report ID: " + safe(report.getReportId()), MARGIN_LEFT, y, PDType1Font.HELVETICA, 11);
                y = writeLine(content, "Property ID: " + safe(report.getPropertyId()), MARGIN_LEFT, y, PDType1Font.HELVETICA, 11);
                y = writeLine(content, "Analyst: " + safe(report.getAnalystId()), MARGIN_LEFT, y, PDType1Font.HELVETICA, 11);
                y -= 8;

                y = writeLine(content, "Included Sections", MARGIN_LEFT, y, PDType1Font.HELVETICA_BOLD, 13);
                for (String section : report.getIncludedSections()) {
                    y = writeLine(content, "- " + section, MARGIN_LEFT + 10, y, PDType1Font.HELVETICA, 11);
                    if ("not_calculated_note".equals(section)) {
                        y = writeLine(content, "  Not calculated", MARGIN_LEFT + 24, y, PDType1Font.HELVETICA_OBLIQUE, 10);
                    }
                }
                y -= 8;

                y = writeLine(content, "Analyst Notes", MARGIN_LEFT, y, PDType1Font.HELVETICA_BOLD, 13);
                List<String> wrapped = wrapText(safe(report.getAnalystNotes()), 90);
                for (String line : wrapped) {
                    y = writeLine(content, line, MARGIN_LEFT + 10, y, PDType1Font.HELVETICA, 11);
                    if (y < 80f) {
                        break;
                    }
                }
                y -= 8;
                y = writeLine(content, "Embedded Charts", MARGIN_LEFT, y, PDType1Font.HELVETICA_BOLD, 13);
                BufferedImage trendChart = buildPlaceholderImage("Price Trend Chart");
                PDImageXObject trendImage = LosslessFactory.createFromImage(document, trendChart);
                content.drawImage(trendImage, MARGIN_LEFT, y - 110, 220, 100);

                BufferedImage heatmapSnap = buildPlaceholderImage("Heatmap Snapshot");
                PDImageXObject heatmapImage = LosslessFactory.createFromImage(document, heatmapSnap);
                content.drawImage(heatmapImage, MARGIN_LEFT + 240, y - 110, 220, 100);
                drawFooter(content, page);
            }

            document.save(outputFile.toFile());
        }

        return outputFile;
    }

    private float writeLine(PDPageContentStream content, String text, float x, float y,
                            PDType1Font font, int size) throws IOException {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
        return y - LINE_GAP;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private List<String> wrapText(String text, int maxChars) {
        if (text == null || text.isBlank()) {
            return List.of("-");
        }
        String[] words = text.trim().split("\\s+");
        StringBuilder line = new StringBuilder();
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        for (String word : words) {
            if (line.length() + word.length() + 1 > maxChars) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                if (!line.isEmpty()) {
                    line.append(' ');
                }
                line.append(word);
            }
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }

    private void drawHeader(PDPageContentStream content, PDPage page) throws IOException {
        content.setNonStrokingColor(30, 41, 59);
        content.addRect(0, page.getMediaBox().getHeight() - 36, page.getMediaBox().getWidth(), 24);
        content.fill();
        content.setNonStrokingColor(255, 255, 255);
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 10);
        content.newLineAtOffset(MARGIN_LEFT, page.getMediaBox().getHeight() - 28);
        content.showText("LOCUS Analytics | Valuation Pack");
        content.endText();
        content.setNonStrokingColor(0, 0, 0);
    }

    private void drawFooter(PDPageContentStream content, PDPage page) throws IOException {
        content.setNonStrokingColor(51, 65, 85);
        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 9);
        content.newLineAtOffset(MARGIN_LEFT, 24);
        content.showText("Confidential - For analyst use only");
        content.endText();
        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 9);
        content.newLineAtOffset(page.getMediaBox().getWidth() - 110, 24);
        content.showText("Page 1");
        content.endText();
        content.setNonStrokingColor(0, 0, 0);
    }

    private BufferedImage buildPlaceholderImage(String title) {
        BufferedImage image = new BufferedImage(640, 320, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(16, 24, 40));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.setColor(new Color(30, 41, 59));
        for (int i = 0; i < 6; i++) {
            graphics.drawLine(0, i * 52, image.getWidth(), i * 52);
        }
        graphics.setColor(new Color(16, 185, 129));
        graphics.setFont(new Font("Arial", Font.BOLD, 28));
        graphics.drawString(title, 24, 42);
        graphics.dispose();
        return image;
    }
}
