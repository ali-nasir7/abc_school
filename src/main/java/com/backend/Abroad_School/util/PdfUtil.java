 package com.backend.Abroad_School.util;

// import com.backend.Abroad_School.model.Student;
// import com.itextpdf.kernel.colors.ColorConstants;
// import com.itextpdf.kernel.font.PdfFontFactory;
// import com.itextpdf.kernel.pdf.PdfDocument;
// import com.itextpdf.kernel.pdf.PdfWriter;
// import com.itextpdf.layout.Document;
// import com.itextpdf.layout.element.Paragraph;
// import com.itextpdf.layout.properties.TextAlignment;

// import org.springframework.stereotype.Component;

// import java.io.ByteArrayOutputStream;
// import java.time.LocalDate;

// @Component
 public class PdfUtil {

//     public byte[] generateAdmissionVoucher(Student student, double admissionFee) {
//         try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

          
//             PdfWriter writer = new PdfWriter(out);
//             PdfDocument pdf = new PdfDocument(writer);
//             Document doc = new Document(pdf);

           
//             Paragraph title = new Paragraph("ADMISSION VOUCHER")
//                     .setFont(PdfFontFactory.createFont())
//                     .setFontSize(18)
//                     .setBold()
//                     .setFontColor(ColorConstants.BLUE)
//                     .setTextAlignment(TextAlignment.CENTER);

         
//             doc.add(title);
//             doc.add(new Paragraph("Date: " + LocalDate.now()));
//             doc.add(new Paragraph("GR Number: " + student.getGrNumber()));
//             doc.add(new Paragraph("Student Name: " + student.getFullName()));
//             doc.add(new Paragraph("Class: " + student.getClassName()));
//             doc.add(new Paragraph("Section: " + student.getSection()));
//             doc.add(new Paragraph("Admission Fee: Rs. " + admissionFee));
//             doc.add(new Paragraph("Status: " + student.getStudentStatus()));
//             doc.add(new Paragraph("\n\nSignature: ______________________"));

          
//             doc.close();

//             return out.toByteArray();

//         } catch (Exception e) {
//             throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
//         }
//     }
 }
