package com.backend.Abroad_School.service;

import com.backend.Abroad_School.dto.StudentDTO;
import com.backend.Abroad_School.exception.ResourceNotFoundException;
import com.backend.Abroad_School.model.Document;
import com.backend.Abroad_School.model.Student;
import com.backend.Abroad_School.model.StudentStatus;
import com.backend.Abroad_School.repository.DocumentRepository;
import com.backend.Abroad_School.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final StudentMapper studentMapper;

    public StudentServiceImpl(StudentRepository studentRepository,
                              DocumentRepository documentRepository,
                              FileStorageService fileStorageService,
                              StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.studentMapper = studentMapper;
    }

    
    @Override
    @Transactional
    public StudentDTO createStudent(StudentDTO dto) {
        Student student = new Student();
        student.setFullName(dto.getFullName());
        student.setFatherName(dto.getFatherName());
        student.setMotherName(dto.getMotherName());
        student.setFatherCnic(dto.getFatherCnic());
        student.setMotherCnic(dto.getMotherCnic());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setClassName(dto.getClassName());
        student.setSection(dto.getSection());
        student.setGroupName(dto.getGroupName());
        student.setAdmissionDate(dto.getAdmissionDate() == null ? LocalDate.now() : dto.getAdmissionDate());
        student.setPreviousSchool(dto.getPreviousSchool());
        student.setParentContact1(dto.getParentContact1());
        student.setParentContact2(dto.getParentContact2());
        student.setAddress(dto.getAddress());
        student.setBFormNumber(dto.getBFormNumber());
        student.setStudentStatus(dto.getStudentStatus() != null ? dto.getStudentStatus() : StudentStatus.ACTIVE);

        Student saved = studentRepository.saveAndFlush(student);

        String grNumber = generateGrNumber(saved.getId());
        saved.setGrNumber(grNumber);

        Integer rollNumber = assignRollNumber(saved.getClassName(), saved.getSection());
        saved.setRollNumber(rollNumber);

        saved = studentRepository.saveAndFlush(saved);
        return studentMapper.toDto(saved);
    }

    private String generateGrNumber(Long id) {
        String year = String.valueOf(LocalDate.now().getYear());
        return String.format("GR-%s-%04d", year, id);
    }

    private Integer assignRollNumber(String className, String section) {
        Integer maxRoll = studentRepository.findMaxRollNumberByClassAndSection(className, section);
        if (maxRoll == null) maxRoll = 0;
        return maxRoll + 1;
    }

    @Override
    public Student getById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    @Override
    public Student getByGrNumber(String grNumber) {
        return studentRepository.findByGrNumber(grNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with GR: " + grNumber));
    }

    @Override
    public List<Student> searchByName(String q) {
        return studentRepository.findByFullNameContainingIgnoreCase(q);
    }

    @Override
    public String storeDocument(Long studentId, MultipartFile file, String type) {
        Student student = getById(studentId);

        Document.DocumentType documentType;
        try {
            documentType = Document.DocumentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid document type: " + type);
        }

        Document document = fileStorageService.storeFile(file, student, documentType);
        documentRepository.save(document);
        return document.getFilePath();
    }

   
    @Transactional(readOnly = true)
    public byte[] generateAdmissionVoucher(Long studentId, Map<String, Double> fees) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Paragraph header = new Paragraph("ABROAD SCHOOL", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BaseColor.DARK_GRAY);
            Paragraph subHeader = new Paragraph("Admission Fee Voucher", subHeaderFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(subHeader);

            document.add(new Paragraph("\n"));

            
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            addRow(infoTable, "Student Name:", student.getFullName());
            addRow(infoTable, "Father Name:", student.getFatherName());
            addRow(infoTable, "Class / Section:", nonNullString(student.getClassName()) + " - " + nonNullString(student.getSection()));
            addRow(infoTable, "GR Number:", student.getGrNumber());
            addRow(infoTable, "Admission Date:", String.valueOf(student.getAdmissionDate()));
            document.add(infoTable);
            document.add(new Paragraph("\n"));

        
            PdfPTable feeTable = new PdfPTable(2);
            feeTable.setWidthPercentage(60);
            feeTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell header1 = new PdfPCell(new Phrase("Particulars", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            header1.setHorizontalAlignment(Element.ALIGN_CENTER);
            header1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            feeTable.addCell(header1);

            PdfPCell header2 = new PdfPCell(new Phrase("Amount (PKR)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            header2.setHorizontalAlignment(Element.ALIGN_CENTER);
            header2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            feeTable.addCell(header2);

            double total = 0;
            for (Map.Entry<String, Double> entry : fees.entrySet()) {
                addFeeRow(feeTable, entry.getKey(), String.format("%.2f", entry.getValue()));
                total += entry.getValue();
            }

            PdfPCell totalLabel = new PdfPCell(new Phrase("Total", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            feeTable.addCell(totalLabel);

            PdfPCell totalValue = new PdfPCell(new Phrase(String.format("%.2f", total), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            totalValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            feeTable.addCell(totalValue);

            document.add(feeTable);
            document.add(new Paragraph("\n\n"));

          
            Paragraph footer = new Paragraph("Authorized Signature: _______________________",
                    FontFactory.getFont(FontFactory.HELVETICA, 11));
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            Paragraph date = new Paragraph("Generated on: " + LocalDate.now(),
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            date.setAlignment(Element.ALIGN_LEFT);
            document.add(date);

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error generating PDF: " + e.getMessage(), e);
        }
    }

 
    private void addRow(PdfPTable table, String label, String value) {
        Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

        PdfPCell c1 = new PdfPCell(new Phrase(label, bold));
        c1.setBorder(PdfPCell.NO_BORDER);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(nonNullString(value), normal));
        c2.setBorder(PdfPCell.NO_BORDER);
        table.addCell(c2);
    }

    private void addFeeRow(PdfPTable table, String label, String value) {
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

        PdfPCell c1 = new PdfPCell(new Phrase(label, normal));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, normal));
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c2);
    }

    private String nonNullString(String s) {
        return s == null ? "" : s;
    }
}
