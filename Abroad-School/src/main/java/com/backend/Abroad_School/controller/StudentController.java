package com.backend.Abroad_School.controller;

import com.backend.Abroad_School.dto.StudentDTO;
import com.backend.Abroad_School.model.Student;
import com.backend.Abroad_School.service.StudentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/create")
    public ResponseEntity<StudentDTO> createStudent(@Validated @RequestBody StudentDTO dto) {
        StudentDTO saved = studentService.createStudent(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getById(id));
    }

    @GetMapping("/gr/{gr}")
    public ResponseEntity<Student> getByGr(@PathVariable("gr") String gr) {
        return ResponseEntity.ok(studentService.getByGrNumber(gr));
    }

   
    @GetMapping("/search")
    public ResponseEntity<List<Student>> search(@RequestParam("q") String q) {
        return ResponseEntity.ok(studentService.searchByName(q));
    }


    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {

        String path = studentService.storeDocument(id, file, type);
        return ResponseEntity.ok(path);
 
    }
@PostMapping("/{id}/admission-voucher")
public ResponseEntity<byte[]> generateAdmissionVoucher(@PathVariable Long id) {
    byte[] pdfBytes = studentService.generateAdmissionVoucher(id);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=admission_voucher.pdf");

    return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
}

@PostMapping("/student/{sid}/assign-plan/{pid}")
public Student assignPlan(@PathVariable Long sid, @PathVariable Long pid) {
    return studentService.assignFeePlan(sid, pid);
}







}