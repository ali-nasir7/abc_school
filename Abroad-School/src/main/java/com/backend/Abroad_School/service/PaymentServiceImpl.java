package com.backend.Abroad_School.service;

import com.backend.Abroad_School.dto.PaymentRequest;
import com.backend.Abroad_School.exception.ResourceNotFoundException;
import com.backend.Abroad_School.model.FeeHead;
import com.backend.Abroad_School.model.FeePlan;
import com.backend.Abroad_School.model.LedgerEntry;
import com.backend.Abroad_School.model.Payment;
import com.backend.Abroad_School.model.Student;
import com.backend.Abroad_School.repository.FeePlanRepository;
import com.backend.Abroad_School.repository.LedgerRepository;
import com.backend.Abroad_School.repository.PaymentRepository;
import com.backend.Abroad_School.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final LedgerRepository ledgerRepository;
    private final StudentRepository studentRepository;
    private final FeePlanRepository feePlanRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              LedgerRepository ledgerRepository,
                              StudentRepository studentRepository,
                              FeePlanRepository feePlanRepository) {
        this.paymentRepository = paymentRepository;
        this.ledgerRepository = ledgerRepository;
        this.studentRepository = studentRepository;
        this.feePlanRepository = feePlanRepository;
    }

    @Override
    @Transactional
public Payment makePayment(Payment payment) {
    payment.setPaymentDate(LocalDate.now());
    Payment saved = paymentRepository.save(payment);

    double planSum = 0;

    if (payment.getFeePlan() != null && payment.getFeePlan().getFeeHeads() != null) {
        System.out.println("Fee Plan: " + payment.getFeePlan());
        System.out.println("Fee Heads: " + payment.getFeePlan().getFeeHeads());
      
        planSum = payment.getFeePlan().getFeeHeads().stream()
                .mapToDouble(FeeHead::getAmount).sum();
    } else {
        System.out.println("Fee Plan or Fee Heads are missing!");
    }

    // Retrieve the ledger for the student
    LedgerEntry ledger = ledgerRepository.findByStudent(payment.getStudent());

    if (ledger == null) {
        // If no ledger exists, create a new one
        ledger = LedgerEntry.builder()
                .student(payment.getStudent())
                .totalPaid(payment.getAmountPaid())
                .totalDue(planSum)
                .balance(planSum - payment.getAmountPaid())
                .lastPaymentDate(LocalDate.now())
                .build();
    } else {
        // Update existing ledger
        ledger.setTotalPaid(ledger.getTotalPaid() + payment.getAmountPaid());
        ledger.setBalance(ledger.getBalance() - payment.getAmountPaid());
        ledger.setLastPaymentDate(LocalDate.now());
    }

    // Save the updated ledger entry
    ledgerRepository.save(ledger);

    // Return the saved payment object
    return saved;
}

    
@Override
@Transactional
public Payment makePayment(PaymentRequest request) {
    Student student = studentRepository.findById(request.getStudentId())
            .orElseThrow(() -> new RuntimeException("Student not found: " + request.getStudentId()));

    FeePlan feePlan = feePlanRepository.findById(request.getFeePlanId())
            .orElseThrow(() -> new RuntimeException("FeePlan not found: " + request.getFeePlanId()));

    Payment payment = new Payment();
    payment.setStudent(student);
    payment.setFeePlan(feePlan);
    payment.setAmountPaid(request.getAmountPaid());
    payment.setDiscount(request.getDiscount());
    payment.setLateFee(0);
    payment.setPaymentDate(LocalDate.now());

    Payment saved = makePayment(payment); 
    return saved;
}


    @Override
    public List<Payment> getPaymentsForStudent(Long studentId) {

        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found: " + studentId
                ));

        return paymentRepository.findByStudentId(studentId);
    }

    @Override
    public void applyLateFee(List<Student> students) {
        LocalDate today = LocalDate.now();
        LocalDate dueDate = LocalDate.of(today.getYear(), today.getMonth(), 10);

        if (today.isAfter(dueDate)) {
            for (Student s : students) {
                s.addLateFee(500.0); 
            }
        }
    }
}
