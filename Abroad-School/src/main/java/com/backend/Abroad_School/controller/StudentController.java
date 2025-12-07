package com.backend.Abroad_School.controller;

import com.backend.Abroad_School.dto.StudentDTO;
import com.backend.Abroad_School.exception.ResourceNotFoundException;
import com.backend.Abroad_School.model.Student;
import com.backend.Abroad_School.model.StudentStatus;
import com.backend.Abroad_School.repository.LedgerRepository;
import com.backend.Abroad_School.repository.StudentRepository;
import com.backend.Abroad_School.service.StudentService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final LedgerRepository ledgerRepository;

    public StudentController(StudentService studentService, StudentRepository studentRepository, LedgerRepository ledgerRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
        this.ledgerRepository = ledgerRepository;
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


@PutMapping("/student/{id}/status")
public ResponseEntity<Student> changeStatus(@PathVariable Long id, @RequestParam StudentStatus status) {
    Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    student.setStudentStatus(status);
    return ResponseEntity.ok(studentRepository.save(student));
}


@GetMapping("/admissions/report")
public ResponseEntity<List<Student>> getAdmissionReport(
        @RequestParam(required = false) String className,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
) {
    List<Student> students;
    if (className != null && start != null && end != null) {
        students = studentRepository.findByClassNameAndAdmissionDateBetween(className, start, end);
    } else if (className != null) {
        students = studentRepository.findByClassName(className);
    } else if (start != null && end != null) {
        students = studentRepository.findByAdmissionDateBetween(start, end);
    } else {
        students = studentRepository.findAll();
    }
    return ResponseEntity.ok(students);
}


@GetMapping("/student/{studentId}/due")
public ResponseEntity<BigDecimal> getStudentDue(@PathVariable Long studentId) {
    Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

    BigDecimal totalDue = ledgerRepository.calculateTotalDueForStudent(student);
    return ResponseEntity.ok(totalDue);
}

}