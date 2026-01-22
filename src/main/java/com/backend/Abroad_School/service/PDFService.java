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

   
    @Transactional(readOnly = true)
    public byte[] generateAdmissionVoucherPDF(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        FeePlan feePlan = student.getFeePlan();
        if (feePlan == null) {
            throw new IllegalStateException("Student has no FeePlan assigned.");
        }

        
        feePlan.getFeeHeads().size();

        
        Map<String, BigDecimal> fees = new LinkedHashMap<>();
        for (FeeHead head : feePlan.getFeeHeads()) {
            fees.put(head.getName(), BigDecimal.valueOf(head.getAmount()));
        }

        return createPDF(student, fees, "Admission Fee Voucher");
    }

  
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

      
        feePlan.getFeeHeads().size();

        Map<String, BigDecimal> fees = new LinkedHashMap<>();
        for (FeeHead head : feePlan.getFeeHeads()) {
            fees.put(head.getName(), BigDecimal.valueOf(head.getAmount()));
        }

        if (voucher.getLateFee() > 0) {
            fees.put("Late Fee", BigDecimal.valueOf(voucher.getLateFee()));
        }

        return createPDF(student, fees, "Admission Fee Voucher (With Late Fee)");
    }

    private byte[] createPDF(Student student, Map<String, BigDecimal> fees, String title) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, out);
        document.open();

        for (int copy = 1; copy <= 2; copy++) { // 1 = School Copy, 2 = Student Copy
         
            try {
                Image logo = Image.getInstance("src/main/resources/static/logo.png"); 
                logo.scaleToFit(80, 80);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                
            }

            Paragraph header = new Paragraph("THE ABROAD SCHOOLING SYSTEM",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph(
                    (copy == 1 ? "School Copy | " : "Student Copy | ") + "Session 2026–2027",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.ORANGE));
            subHeader.setAlignment(Element.ALIGN_LEFT);
            document.add(subHeader);
            document.add(Chunk.NEWLINE);

            PdfPTable infoTable = new PdfPTable(new float[]{2, 3, 2, 2});
            infoTable.setWidthPercentage(100);
            addInfoCell(infoTable, "Student Name", student.getFullName());
            addInfoCell(infoTable, "Class", student.getClassName());
            addInfoCell(infoTable, "Father Name", student.getFatherName());
            addInfoCell(infoTable, "Voucher No", safe(student.getGrNumber()));
            addInfoCell(infoTable, "Due Date", safe(student.getAdmissionDate() != null ? student.getAdmissionDate().toString() : ""));
            addInfoCell(infoTable, "Issue Date", java.time.LocalDate.now().toString());
            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            PdfPTable feeTable = new PdfPTable(new float[]{4, 2});
            feeTable.setWidthPercentage(80);
            PdfPCell h1 = new PdfPCell(new Phrase("Fee Description", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            h1.setHorizontalAlignment(Element.ALIGN_CENTER);
            h1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            feeTable.addCell(h1);
            PdfPCell h2 = new PdfPCell(new Phrase("Amount (Rs.)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            h2.setHorizontalAlignment(Element.ALIGN_CENTER);
            h2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            feeTable.addCell(h2);

            BigDecimal total = BigDecimal.ZERO;
            for (Map.Entry<String, BigDecimal> entry : fees.entrySet()) {
                PdfPCell c1 = new PdfPCell(new Phrase(entry.getKey()));
                PdfPCell c2 = new PdfPCell(new Phrase(entry.getValue().toPlainString()));
                feeTable.addCell(c1);
                feeTable.addCell(c2);
                total = total.add(entry.getValue());
            }

          
            PdfPCell totalLabel = new PdfPCell(new Phrase("Total Payable", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            feeTable.addCell(totalLabel);
            PdfPCell totalValue = new PdfPCell(new Phrase(total.toPlainString(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            totalValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            feeTable.addCell(totalValue);

            document.add(feeTable);
            document.add(Chunk.NEWLINE);

       
            Paragraph footer = new Paragraph(
                    "Contact: 0339-4016714 / 0330-3692701\n" +
                    "Accountant: ________   Principal: ________",
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            footer.setAlignment(Element.ALIGN_LEFT);
            document.add(footer);

            if (copy == 1) { 
                document.add(new Paragraph("\n\n----------------------------------------\n\n"));
            }
        }

        document.close();
        return out.toByteArray();
    } catch (Exception e) {
        throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
    }
}

private void addInfoCell(PdfPTable table, String label, String value) {
    table.addCell(new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11))));
    table.addCell(new PdfPCell(new Phrase(safe(value), FontFactory.getFont(FontFactory.HELVETICA, 11))));
}
private String safe(String s) {
    return s == null ? "" : s;
}


    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) { super(message); }
    }

@Transactional(readOnly = true)
public byte[] generateVoucherPDF(Voucher voucher) {
    if (voucher == null || voucher.getStudent() == null) {
        throw new IllegalArgumentException("Voucher or Student cannot be null.");
    }

    Student student = voucher.getStudent();
    FeePlan feePlan = student.getFeePlan();
    if (feePlan == null) {
        throw new IllegalStateException("Student has no FeePlan assigned.");
    }

    feePlan.getFeeHeads().size();

    Map<String, BigDecimal> fees = new java.util.LinkedHashMap<>();
    for (FeeHead head : feePlan.getFeeHeads()) {
        fees.put(head.getName(), BigDecimal.valueOf(head.getAmount()));
    }

    if (voucher.getLateFee() > 0) fees.put("Late Fee", BigDecimal.valueOf(voucher.getLateFee()));

    if (voucher.getDiscount() > 0) fees.put("Discount", BigDecimal.valueOf(-Math.abs(voucher.getDiscount())));

    return createPDF(student, fees, "Fee Voucher");
}


}
