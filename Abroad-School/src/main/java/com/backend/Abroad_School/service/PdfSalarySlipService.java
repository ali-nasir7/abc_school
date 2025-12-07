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
        Payroll p = payrollRepository.findById(payrollId).orElseThrow(() -> new RuntimeException("Payroll not found: " + payrollId));
        Staff s = p.getStaff();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph t = new Paragraph("Salary Slip", title);
            t.setAlignment(Element.ALIGN_CENTER);
            document.add(t);
            document.add(Chunk.NEWLINE);

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);

            addCell(info, "Staff Name:");
            addCell(info, s.getFullName());
            addCell(info, "Designation:");
            addCell(info, s.getDesignation().name());
            addCell(info, "Payroll Period:");
            addCell(info, p.getPeriodStart().format(DateTimeFormatter.ISO_DATE) + " to " + p.getPeriodEnd().format(DateTimeFormatter.ISO_DATE));
            addCell(info, "Processed On:");
            addCell(info, p.getProcessedAt().format(DateTimeFormatter.ISO_DATE));
            document.add(info);
            document.add(Chunk.NEWLINE);

            PdfPTable t2 = new PdfPTable(2);
            t2.setWidthPercentage(100);
            addHeaderCell(t2, "Particular");
            addHeaderCell(t2, "Amount (PKR)");

            addCell(t2, "Basic Pay");
            addCell(t2, p.getGrossAmount().subtract(p.getTotalAllowances()).toString());

            addCell(t2, "Allowances");
            addCell(t2, p.getTotalAllowances().toString());

            addCell(t2, "Deductions");
            addCell(t2, p.getTotalDeductions().toString());

            addCell(t2, "Tax");
            addCell(t2, p.getTax().toString());

            addHeaderCell(t2, "Net Pay");
            addHeaderCell(t2, p.getNetPay().toString());

            document.add(t2);
            document.add(Chunk.NEWLINE);

            Paragraph remarks = new Paragraph("Remarks: " + (p.getRemarks() == null ? "" : p.getRemarks()));
            document.add(remarks);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate salary slip: " + e.getMessage(), e);
        }
    }

    private void addCell(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text));
        c.setBorder(PdfPCell.NO_BORDER);
        table.addCell(c);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        table.addCell(c);
    }
}
