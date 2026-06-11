package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico.FilaReporte;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico.ReporteDinamico;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico.ReporteDinamicoInput;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ReporteExportacionService {

    private final ReporteDinamicoService reporteDinamicoService;

    public String exportarPdf(ReporteDinamicoInput input) {
        ReporteDinamico reporte = reporteDinamicoService.ejecutar(input);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (Document document = new Document()) {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph(reporte.getTitulo(), titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            
            PdfPCell hCell1 = new PdfPCell(new Phrase(reporte.getEtiquetaDimension(), headFont));
            hCell1.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            table.addCell(hCell1);
            
            PdfPCell hCell2 = new PdfPCell(new Phrase(reporte.getEtiquetaMetrica(), headFont));
            hCell2.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            table.addCell(hCell2);

            for (FilaReporte fila : reporte.getFilas()) {
                table.addCell(String.valueOf(fila.getEtiqueta()));
                table.addCell(String.valueOf(fila.getValor()));
            }

            PdfPCell fCell1 = new PdfPCell(new Phrase("TOTAL", headFont));
            fCell1.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            table.addCell(fCell1);
            
            PdfPCell fCell2 = new PdfPCell(new Phrase(String.valueOf(reporte.getTotal()), headFont));
            table.addCell(fCell2);

            document.add(table);
        }

        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    public String exportarExcel(ReporteDinamicoInput input) {
        ReporteDinamico reporte = reporteDinamicoService.ejecutar(input);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte");
            
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(reporte.getTitulo());

            Row headerRow = sheet.createRow(2);
            headerRow.createCell(0).setCellValue(reporte.getEtiquetaDimension());
            headerRow.createCell(1).setCellValue(reporte.getEtiquetaMetrica());

            int rowIdx = 3;
            for (FilaReporte fila : reporte.getFilas()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(fila.getEtiqueta());
                row.createCell(1).setCellValue(fila.getValor());
            }

            Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(0).setCellValue("TOTAL");
            totalRow.createCell(1).setCellValue(reporte.getTotal());

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage(), e);
        }
    }
}
