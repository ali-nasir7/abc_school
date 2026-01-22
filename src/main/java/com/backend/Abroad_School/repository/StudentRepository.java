package com.backend.Abroad_School.repository;

import com.backend.Abroad_School.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByGrNumber(String grNumber);
    Optional<Student> findByFatherCnic(String cnic);
    Optional<Student> findByParentContact1(String phone);
    List<Student> findByFullNameContainingIgnoreCase(String name);
    @Query("SELECT MAX(s.rollNumber) FROM Student s WHERE s.className = :className AND s.section = :section")
Integer findMaxRollNumberByClassAndSection(@Param("className") String className, @Param("section") String section);
List<Student> findByClassName(String className);
List<Student> findByAdmissionDateBetween(LocalDate start, LocalDate end);
List<Student> findByClassNameAndAdmissionDateBetween(String className, LocalDate start, LocalDate end);



}