package com.backend.Abroad_School.repository;

import com.backend.Abroad_School.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByStaffId(Long staffId);
    List<Payroll> findByPeriodStartBetween(LocalDate start, LocalDate end);
    List<Payroll> findAll();
}
