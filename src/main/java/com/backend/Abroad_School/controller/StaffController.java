package com.backend.Abroad_School.controller;

import com.backend.Abroad_School.dto.PayrollRequestDTO;
import com.backend.Abroad_School.dto.SalaryStructureDTO;
import com.backend.Abroad_School.dto.StaffDTO;
import com.backend.Abroad_School.model.Payroll;
import com.backend.Abroad_School.model.SalaryStructure;
import com.backend.Abroad_School.model.Staff;
import com.backend.Abroad_School.repository.PayrollRepository;
import com.backend.Abroad_School.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;
    private final SalaryStructureService salaryStructureService;
    private final PayrollService payrollService;
    private final PdfSalarySlipService pdfSalarySlipService;
    private final PayrollRepository payrollRepository;

    @PostMapping("/create")
    public ResponseEntity<StaffDTO> createStaff(@Valid @RequestBody StaffDTO dto) {
        return ResponseEntity.ok(staffService.createStaff(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffDTO> updateStaff(@PathVariable Long id, @Valid @RequestBody StaffDTO dto) {
        return ResponseEntity.ok(staffService.updateStaff(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Staff> getStaff(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.getStaff(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Staff>> activeStaff() {
        return ResponseEntity.ok(staffService.listAllActiveStaff());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        staffService.deactivateStaff(id);
        return ResponseEntity.noContent().build();
    }

    // Salary structure
    @PostMapping("/salary-structure")
    public ResponseEntity<SalaryStructureDTO> createSalaryStructure(@Valid @RequestBody SalaryStructureDTO dto) {
        return ResponseEntity.ok(salaryStructureService.create(dto));
    }

    @PutMapping("/salary-structure/{id}")
    public ResponseEntity<SalaryStructureDTO> updateSalaryStructure(@PathVariable Long id, @Valid @RequestBody SalaryStructureDTO dto) {
        return ResponseEntity.ok(salaryStructureService.update(id, dto));
    }

    @GetMapping("/salary-structure")
    public ResponseEntity<List<SalaryStructure>> listSalaryStructures() {
        return ResponseEntity.ok(salaryStructureService.listAll());
    }

    // Payroll
    @PostMapping("/payroll/process")
    public ResponseEntity<Payroll> processPayroll(@Valid @RequestBody PayrollRequestDTO req) {
        Payroll p = payrollService.processPayroll(req);
        return ResponseEntity.ok(p);
    }

    @GetMapping("/payroll/staff/{staffId}")
    public ResponseEntity<List<Payroll>> payrollsForStaff(@PathVariable Long staffId) {
        return ResponseEntity.ok(payrollService.getPayrollsForStaff(staffId));
    }

    @GetMapping("/payroll/{id}")
    public ResponseEntity<Payroll> getPayroll(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.getPayroll(id));
    }

    @GetMapping("/payroll/{id}/slip")
    public ResponseEntity<byte[]> payrollSlip(@PathVariable Long id) {
        byte[] pdf = pdfSalarySlipService.generateSalarySlip(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=salary_slip_" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
    @GetMapping("/payroll/all")
    public ResponseEntity <List<Payroll>> getAllPayrolls(){
        return ResponseEntity.ok(payrollRepository.findAll());
    }
}
