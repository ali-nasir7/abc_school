package com.backend.Abroad_School.repository;



import com.backend.Abroad_School.model.LedgerEntry;
import com.backend.Abroad_School.model.Student;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {
    LedgerEntry findByStudent(Student student);
    @Query("SELECT SUM(l.amountDue) FROM LedgerEntry l WHERE l.student = :student")
BigDecimal calculateTotalDueForStudent(@Param("student") Student student);
}
