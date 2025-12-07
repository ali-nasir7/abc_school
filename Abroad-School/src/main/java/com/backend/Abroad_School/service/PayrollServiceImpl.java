package com.backend.Abroad_School.service;
import com.backend.Abroad_School.dto.PayrollRequestDTO;
import com.backend.Abroad_School.exception.ResourceNotFoundException;
import com.backend.Abroad_School.model.Payroll;
import com.backend.Abroad_School.model.SalaryStructure;
import com.backend.Abroad_School.model.Staff;
import com.backend.Abroad_School.repository.PayrollRepository;
import com.backend.Abroad_School.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollServiceImpl implements PayrollService {

    private final StaffRepository staffRepository;
    private final PayrollRepository payrollRepository;

    @Override
    public Payroll processPayroll(PayrollRequestDTO request) {
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + request.getStaffId()));

        SalaryStructure structure = staff.getSalaryStructure();
        if (structure == null) {
            throw new IllegalStateException("Staff has no salary structure assigned: " + staff.getId());
        }

        BigDecimal basic = safe(structure.getBasicPay());
        BigDecimal allowances = safe(structure.getAllowances());
        BigDecimal deductions = safe(structure.getDeductions());
        BigDecimal tax = safe(structure.getTax());

        BigDecimal gross = basic.add(allowances);
        BigDecimal net = gross.subtract(deductions).subtract(tax);
        if (net.compareTo(BigDecimal.ZERO) < 0) net = BigDecimal.ZERO;

        Payroll p = Payroll.builder()
                .staff(staff)
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .grossAmount(gross)
                .totalAllowances(allowances)
                .totalDeductions(deductions)
                .tax(tax)
                .netPay(net)
                .processedAt(LocalDate.now())
                .remarks(request.getRemarks())
                .build();

        return payrollRepository.save(p);
    }

    @Override
    public List<Payroll> getPayrollsForStaff(Long staffId) {
        return payrollRepository.findByStaffId(staffId);
    }

    @Override
    public Payroll getPayroll(Long id) {
        return payrollRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payroll not found: " + id));
    }

    private BigDecimal safe(BigDecimal b) {
        return b == null ? BigDecimal.ZERO : b;
    }
}
