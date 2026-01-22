package com.backend.Abroad_School.service;

import com.backend.Abroad_School.model.Payroll;
import com.backend.Abroad_School.model.Staff;
import com.backend.Abroad_School.repository.PayrollRepository;
import com.backend.Abroad_School.repository.StaffRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfSalarySlipService {

    private final PayrollRepository payrollRepository;
    private final StaffRepository staffRepository;

   public byte[] generateSalarySlip(Long payrollId) {
    Payroll p = payrollRepository.findById(payrollId)
            .orElseThrow(() -> new RuntimeException("Payroll not found: " + payrollId));
    Staff s = p.getStaff();

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

        Document document = new Document(PageSize.A4, 36, 36, 30, 36);
        PdfWriter.getInstance(document, out);
        document.open();

        // ===== LOGO =====
        Image logo = Image.getInstance("src/main/resources/static/logo.jpg");
        logo.scaleToFit(80, 80);
        logo.setAlignment(Image.ALIGN_CENTER);
        document.add(logo);

        // ===== SCHOOL NAME =====
        Font schoolFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18,
                new BaseColor(31, 58, 95)); // dark blue
        Paragraph school = new Paragraph("The Abroad Schooling System", schoolFont);
        school.setAlignment(Element.ALIGN_CENTER);
        document.add(school);

        // ===== TITLE =====
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph title = new Paragraph("Salary Slip", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // ===== STAFF INFO TABLE =====
        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setSpacingBefore(10);
        info.setWidths(new float[]{1.5f, 2.5f});

        addInfoCell(info, "Teacher Name", s.getFullName());
        addInfoCell(info, "Designation", s.getDesignation().name());
        addInfoCell(info, "Payroll Period",
                p.getPeriodStart() + " to " + p.getPeriodEnd());
        addInfoCell(info, "Processed On", p.getProcessedAt().toString());

        document.add(info);
        document.add(Chunk.NEWLINE);

        // ===== SALARY TABLE =====
        PdfPTable salary = new PdfPTable(2);
        salary.setWidthPercentage(100);
        salary.setSpacingBefore(10);

        addTableHeader(salary, "Description");
        addTableHeader(salary, "Amount (PKR)");

        addRow(salary, "Basic Salary",
                p.getGrossAmount().subtract(p.getTotalAllowances()));
        addRow(salary, "Allowances", p.getTotalAllowances());
        addRow(salary, "Deductions", p.getTotalDeductions());
        addRow(salary, "Tax", p.getTax());

        document.add(salary);

        // ===== NET PAY BOX =====
        PdfPTable netPay = new PdfPTable(2);
        netPay.setWidthPercentage(60);
        netPay.setSpacingBefore(15);
        netPay.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell netLabel = new PdfPCell(new Phrase("NET PAY",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        netLabel.setBackgroundColor(new BaseColor(220, 240, 220));
        netLabel.setPadding(8);

        PdfPCell netAmount = new PdfPCell(new Phrase(
                p.getNetPay().toString(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        netAmount.setBackgroundColor(new BaseColor(220, 240, 220));
        netAmount.setPadding(8);

        netPay.addCell(netLabel);
        netPay.addCell(netAmount);
        document.add(netPay);

        // ===== REMARKS =====
        document.add(Chunk.NEWLINE);
        Paragraph remarks = new Paragraph(
                "Remarks: " + (p.getRemarks() == null ? "—" : p.getRemarks()));
        remarks.setFont(FontFactory.getFont(FontFactory.HELVETICA, 10));
        document.add(remarks);

        // ===== FOOTER =====
        document.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
                "This is a system generated salary slip.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();

    } catch (Exception e) {
        throw new RuntimeException("Failed to generate salary slip", e);
    }
    
}
private void addInfoCell(PdfPTable table, String label, String value) {
    PdfPCell labelCell = new PdfPCell(
            new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10))
    );
    labelCell.setBorder(Rectangle.NO_BORDER);
    labelCell.setPadding(6);

    PdfPCell valueCell = new PdfPCell(new Phrase(value));
    valueCell.setBorder(Rectangle.NO_BORDER);
    valueCell.setPadding(6);

    table.addCell(labelCell);
    table.addCell(valueCell);
}
private void addTableHeader(PdfPTable table, String text) {
    PdfPCell header = new PdfPCell(
            new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11))
    );
    header.setBackgroundColor(new BaseColor(230, 230, 230));
    header.setPadding(8);
    table.addCell(header);
}
private void addRow(PdfPTable table, String label, Object value) {
    PdfPCell c1 = new PdfPCell(new Phrase(label));
    c1.setPadding(6);

    PdfPCell c2 = new PdfPCell(new Phrase(value.toString()));
    c2.setPadding(6);

    table.addCell(c1);
    table.addCell(c2);
}



}
