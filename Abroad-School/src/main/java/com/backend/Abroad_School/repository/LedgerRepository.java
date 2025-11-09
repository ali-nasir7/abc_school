package com.backend.Abroad_School.repository;



import com.backend.Abroad_School.model.LedgerEntry;
import com.backend.Abroad_School.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {
    LedgerEntry findByStudent(Student student);
}
