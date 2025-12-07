package com.backend.Abroad_School.service;

import com.backend.Abroad_School.model.Student;
import com.backend.Abroad_School.model.FeePlan;
import com.backend.Abroad_School.model.FeeHead;
import com.backend.Abroad_School.model.Voucher;
import com.backend.Abroad_School.repository.StudentRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PDFService {

    private final StudentRepository studentRepository;

    public PDFService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Main Method to Generate Admission Voucher
    @Transactional(readOnly = true)
    public byte[] generateAdmissionVoucherPDF(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        FeePlan feePlan = student.getFeePlan();
        if (feePlan == null) {
            throw new RuntimeException("Student has no FeePlan assigned.");
        }

        // Fee Heads Map
        Map<String, Double> fees = new LinkedHashMap<>();
        for (FeeHead head : feePlan.getFeeHeads()) {
            fees.put(head.getName(), head.getAmount());
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            // Header
            Paragraph header = new Paragraph("KALE KA SCHOOL",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Admission Fee Voucher",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
            subHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(subHeader);

            document.add(new Paragraph("\n"));

            // Student info
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            addRow(infoTable, "Student Name:", student.getFullName());
            addRow(infoTable, "Father Name:", student.getFatherName());
            addRow(infoTable, "Class / Section: ",
                    nonNull(student.getClassName()) + " - " + nonNull(student.getSection()));
            addRow(infoTable, "GR Number:", student.getGrNumber());
            addRow(infoTable, "Admission Date:", nonNull(student.getAdmissionDate() + ""));

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // Fee Table
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

            double total = 0;
            for (Map.Entry<String, Double> entry : fees.entrySet()) {
                addFeeRow(feeTable, entry.getKey(), String.format("%.2f", entry.getValue()));
                total += entry.getValue();
            }

            PdfPCell totalLabel = new PdfPCell(new Phrase("Total", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            feeTable.addCell(totalLabel);

            PdfPCell totalValue = new PdfPCell(new Phrase(String.format("%.2f", total),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            totalValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            feeTable.addCell(totalValue);

            document.add(feeTable);
            document.add(new Paragraph("\n\n"));

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

    // Helper Methods
    private void addRow(PdfPTable table, String label, String value) {
        table.addCell(new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11))));  // For the label
        table.addCell(new PdfPCell(new Phrase(nonNull(value), FontFactory.getFont(FontFactory.HELVETICA, 11))));  // For the value
    }

    private void addFeeRow(PdfPTable table, String label, String value) {
        table.addCell(new PdfPCell(new Phrase(label))); // Fee name
        table.addCell(new PdfPCell(new Phrase(value))); // Fee amount
    }

    private String nonNull(String s) {
        return s == null ? "" : s;  // Check if value is null and return empty string if it is
    }

    // Method to generate PDF for voucher with late fee included
    @Transactional
    public byte[] generateVoucherWithLateFeePDF(Voucher voucher) {
        if (voucher == null || voucher.getStudent() == null) {
            throw new RuntimeException("Voucher or student is null");
        }

        // Get student details and fee plan
        Student student = voucher.getStudent();
        FeePlan feePlan = student.getFeePlan();
        if (feePlan == null) {
            throw new RuntimeException("Student has no FeePlan assigned.");
        }

        // Fee Heads Map
        Map<String, Double> fees = new LinkedHashMap<>();
        for (FeeHead head : feePlan.getFeeHeads()) {
            fees.put(head.getName(), head.getAmount());
        }

        // Add late fee if applied
 if (voucher != null && voucher.getLateFee() > 0) {
    fees.put("Late Fee", (double) voucher.getLateFee());
}



        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            // Header
            Paragraph header = new Paragraph("KALE KA SCHOOL",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Admission Fee Voucher (With Late Fee)",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
            subHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(subHeader);

            document.add(new Paragraph("\n"));

            // Student info
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            addRow(infoTable, "Student Name:", student.getFullName());
            addRow(infoTable, "Father Name:", student.getFatherName());
            addRow(infoTable, "Class / Section:",
                    nonNull(student.getClassName()) + " - " + nonNull(student.getSection()));
            addRow(infoTable, "GR Number:", student.getGrNumber());
            addRow(infoTable, "Admission Date:", nonNull(student.getAdmissionDate() + ""));

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // Fee Table
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

            double total = 0;
            for (Map.Entry<String, Double> entry : fees.entrySet()) {
                addFeeRow(feeTable, entry.getKey(), String.format("%.2f", entry.getValue()));
                total += entry.getValue();
            }

            PdfPCell totalLabel = new PdfPCell(new Phrase("Total", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            feeTable.addCell(totalLabel);

            PdfPCell totalValue = new PdfPCell(new Phrase(String.format("%.2f", total),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            totalValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            feeTable.addCell(totalValue);

            document.add(feeTable);
            document.add(new Paragraph("\n\n"));

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
}
