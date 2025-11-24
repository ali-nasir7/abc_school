package com.backend.Abroad_School.service;

import com.backend.Abroad_School.model.Student;
import com.backend.Abroad_School.repository.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class PDFService {

    private final StudentRepository studentRepository;

    public PDFService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Generates PDF for Admission Voucher
    public byte[] generateAdmissionVoucherPDF(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

      
        return ("Voucher PDF for student: " );//+ student.getName()).getBytes(); // pending
    }

    // Can add more PDF generation methods here in future (FeeReceipt, Certificate, etc.)
}
