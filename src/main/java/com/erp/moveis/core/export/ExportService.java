package com.erp.moveis.core.export;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

    public void exportToPdf(HttpServletResponse response, String title, List<String> headers,
                            List<List<String>> rows, String filePrefix) throws IOException {
        response.setContentType("application/pdf");
        String filename = filePrefix + "_" + TIMESTAMP_FMT.format(LocalDateTime.now()) + ".pdf";
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY);
        Paragraph titleParagraph = new Paragraph(title, titleFont);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        titleParagraph.setSpacingAfter(20);
        document.add(titleParagraph);

        // Timestamp
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
        Paragraph timestamp = new Paragraph("Gerado em: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), smallFont);
        timestamp.setAlignment(Element.ALIGN_RIGHT);
        timestamp.setSpacingAfter(10);
        document.add(timestamp);

        // Table
        PdfPTable table = new PdfPTable(headers.size());
        table.setWidthPercentage(100);

        // Header row
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(41, 128, 185));
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Data rows
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        boolean alternate = false;
        for (List<String> row : rows) {
            for (String value : row) {
                PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", dataFont));
                if (alternate) {
                    cell.setBackgroundColor(new Color(235, 245, 251));
                }
                cell.setPadding(6);
                table.addCell(cell);
            }
            alternate = !alternate;
        }

        document.add(table);

        // Footer
        Paragraph footer = new Paragraph("Total: " + rows.size() + " registros", smallFont);
        footer.setSpacingBefore(10);
        document.add(footer);

        document.close();
    }

    public void exportToExcel(HttpServletResponse response, String sheetName, List<String> headers,
                              List<List<String>> rows, String filePrefix) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String filename = filePrefix + "_" + TIMESTAMP_FMT.format(LocalDateTime.now()) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerPOIFont = workbook.createFont();
            headerPOIFont.setBold(true);
            headerPOIFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerPOIFont);
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Header row
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (int i = 0; i < rows.size(); i++) {
                org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(i + 1);
                List<String> row = rows.get(i);
                for (int j = 0; j < row.size(); j++) {
                    org.apache.poi.ss.usermodel.Cell cell = dataRow.createCell(j);
                    String value = row.get(j);
                    if (value != null) {
                        try {
                            double numValue = Double.parseDouble(value);
                            cell.setCellValue(numValue);
                        } catch (NumberFormatException e) {
                            cell.setCellValue(value);
                        }
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }
}
