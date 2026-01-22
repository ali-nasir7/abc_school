package com.backend.Abroad_School.service;

import com.backend.Abroad_School.dto.PayrollRequestDTO;
import com.backend.Abroad_School.model.Payroll;

import java.util.List;

public interface PayrollService {
    Payroll processPayroll(PayrollRequestDTO request);
    List<Payroll> getPayrollsForStaff(Long staffId);
    Payroll getPayroll(Long id);
}
