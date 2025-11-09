package com.backend.Abroad_School.repository;


import com.backend.Abroad_School.model.Payment;
import com.backend.Abroad_School.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudent(Student student);
}
