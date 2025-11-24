package com.backend.Abroad_School.service;

import com.backend.Abroad_School.dto.PaymentRequest;
import com.backend.Abroad_School.model.Payment;
import com.backend.Abroad_School.model.Student;

import java.util.List;

public interface PaymentService {
    
    Payment makePayment(Payment payment);

    
    Payment makePayment(PaymentRequest request);

   
    List<Payment> getPaymentsForStudent(Long studentId);

    
    void applyLateFee(List<Student> students);
}
