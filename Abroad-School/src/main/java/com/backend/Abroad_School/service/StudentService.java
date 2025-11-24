package com.backend.Abroad_School.service;


import com.backend.Abroad_School.dto.StudentDTO;
import com.backend.Abroad_School.model.Student;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface StudentService {
    StudentDTO createStudent(StudentDTO dto);
    Student getById(Long id);
    Student getByGrNumber(String grNumber);
    List<Student> searchByName(String q);

    Student assignFeePlan(Long studentId, Long feePlanId);
    String storeDocument(Long studentId, MultipartFile file, String type);
    

   //public byte[] generateAdmissionVoucher(Long studentId, Map<String, Double> fees);
    public byte[] generateAdmissionVoucher(Long studentId);
}