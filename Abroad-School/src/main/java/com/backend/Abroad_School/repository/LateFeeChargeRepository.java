package com.backend.Abroad_School.repository;

import com.backend.Abroad_School.model.LateFeeCharge;
import com.backend.Abroad_School.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LateFeeChargeRepository extends JpaRepository<LateFeeCharge, Long> {
    Optional<LateFeeCharge> findByStudentAndMonthYear(Student student, String monthYear);
}
