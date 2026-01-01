package com.backend.Abroad_School.service;

import com.backend.Abroad_School.model.Student;
import com.backend.Abroad_School.model.FeePlan;
import com.backend.Abroad_School.model.FeeHead;
import com.backend.Abroad_School.model.Voucher;
import com.backend.Abroad_School.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

@Service
public class PDFService {

    private final StudentRepository studentRepository;

    public PDFService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // -----------------------------
    // Generate Admission Voucher PDF
    // -----------------------------
    @Transactional(readOnly = true)
    public byte[] generateAdmissionVoucherPDF(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        FeePlan feePlan = student.getFeePlan();
        if (feePlan == null) {
            throw new IllegalStateException("Student has no FeePlan assigned.");
        }

        // Force initialize feeHeads to avoid lazy loading issues
        feePlan.getFeeHeads().size();

        // Use BigDecimal for financial precision
        Map<String, BigDecimal> fees = new LinkedHashMap<>();
        for (FeeHead head : feePlan.getFeeHeads()) {
            fees.put(head.getName(), BigDecimal.valueOf(head.getAmount()));
        }

        return createPDF(student, fees, "Admission Fee Voucher");
    }

    // -----------------------------
    // Generate Voucher PDF with Late Fee
    // -----------------------------
    @Transactional(readOnly = true)
    public byte[] generateVoucherWithLateFeePDF(Voucher voucher) {
        if (voucher == null || voucher.getStudent() == null) {
            throw new IllegalArgumentException("Voucher or Student cannot be null.");
        }

        Student student = voucher.getStudent();
        FeePlan feePlan = student.getFeePlan();
        if (feePlan == null) {
            throw new IllegalStateException("Student has no FeePlan assigned.");
        }

        // Force initialize feeHeads to avoid lazy loading
        feePlan.getFeeHeads().size();

        Map<String, BigDecimal> fees = new LinkedHashMap<>();
        for (FeeHead head : feePlan.getFeeHeads()) {
            fees.put(head.getName(), BigDecimal.valueOf(head.getAmount()));
        }

        // Add late fee if applicable
        if (voucher.getLateFee() > 0) {
            fees.put("Late Fee", BigDecimal.valueOf(voucher.getLateFee()));
        }

        return createPDF(student, fees, "Admission Fee Voucher (With Late Fee)");
    }

    // -----------------------------
    // Private Helper: PDF Creation
    // -----------------------------
    private byte[] createPDF(Student student, Map<String, BigDecimal> fees, String title) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            // Header
            Paragraph header = new Paragraph("KALE KA SCHOOL",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph(title,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
            subHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(subHeader);

            document.add(new Paragraph("\n"));

            // Student info table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            addRow(infoTable, "Student Name:", student.getFullName());
            addRow(infoTable, "Father Name:", student.getFatherName());
            addRow(infoTable, "Class / Section:", safe(student.getClassName()) + " - " + safe(student.getSection()));
            addRow(infoTable, "GR Number:", student.getGrNumber());
            addRow(infoTable, "Admission Date:", student.getAdmissionDate() != null ? student.getAdmissionDate().toString() : "");
            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // Fee table
            PdfPTable feeTable = new PdfPTable(2);
            feeTable.setWidthPercentage(60);
            feeTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell h1 = new PdfPCell(new Phrase("Particulars", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            h1.setHorizontalAlignment(Element.ALIGN_CENTER);
            h1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            feeTable.addCell(h1);

            PdfPCell h2 = new PdfPCell(new Phrase("Amount (PKR)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            h2.setHorizontalAlignment(Element.ALIGN_CENTER);
            h2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            feeTable.addCell(h2);

            BigDecimal total = BigDecimal.ZERO;
            for (Map.Entry<String, BigDecimal> entry : fees.entrySet()) {
                addFeeRow(feeTable, entry.getKey(), entry.getValue().toPlainString());
                total = total.add(entry.getValue());
            }

            PdfPCell totalLabel = new PdfPCell(new Phrase("Total", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            feeTable.addCell(totalLabel);

            PdfPCell totalValue = new PdfPCell(new Phrase(total.toPlainString(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            totalValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            feeTable.addCell(totalValue);

            document.add(feeTable);
            document.add(new Paragraph("\n\n"));

            // Footer
            Paragraph footer = new Paragraph("Authorized Signature: _______________________",
                    FontFactory.getFont(FontFactory.HELVETICA, 11));
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            Paragraph date = new Paragraph("Generated on: " + java.time.LocalDate.now(),
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            date.setAlignment(Element.ALIGN_LEFT);
            document.add(date);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    // -----------------------------
    // Helpers
    // -----------------------------
    private void addRow(PdfPTable table, String label, String value) {
        table.addCell(new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11))));
        table.addCell(new PdfPCell(new Phrase(safe(value), FontFactory.getFont(FontFactory.HELVETICA, 11))));
    }

    private void addFeeRow(PdfPTable table, String label, String value) {
        table.addCell(new PdfPCell(new Phrase(label)));
        table.addCell(new PdfPCell(new Phrase(value)));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // -----------------------------
    // Custom Exception
    // -----------------------------
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) { super(message); }
    }

}
