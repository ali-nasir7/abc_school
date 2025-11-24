package com.backend.Abroad_School.controller;

import com.backend.Abroad_School.model.LedgerEntry;
import com.backend.Abroad_School.model.Student;
import com.backend.Abroad_School.repository.LedgerRepository;
import com.backend.Abroad_School.repository.StudentRepository;
import com.backend.Abroad_School.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerRepository ledgerRepository;
    private final StudentRepository studentRepository;

    public LedgerController(LedgerRepository ledgerRepository, StudentRepository studentRepository) {
        this.ledgerRepository = ledgerRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/student/{studentId}")
    public LedgerEntry getLedger(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        LedgerEntry ledger = ledgerRepository.findByStudent(student);
        if (ledger == null) {
            throw new ResourceNotFoundException("Ledger not found for student: " + studentId);
        }
        return ledger;
    }
}
