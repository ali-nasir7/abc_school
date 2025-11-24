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

        if (payment.getStudent() == null) {
            throw new IllegalArgumentException("No Student found!");
        }

      
        payment.setPaymentDate(LocalDate.now());

       
        Payment saved = paymentRepository.save(payment);

        Student student = saved.getStudent();
        LedgerEntry ledger = ledgerRepository.findByStudent(student);

        
        double planTotal = 0.0;
        FeePlan plan = saved.getFeePlan();

        if (plan != null && plan.getFeeHeads() != null) {
            planTotal = plan.getFeeHeads().stream().mapToDouble(FeeHead::getAmount).sum();
        } else if (student.getFeePlan() != null && student.getFeePlan().getFeeHeads() != null) {
            planTotal = student.getFeePlan().getFeeHeads().stream().mapToDouble(FeeHead::getAmount).sum();
        }

       
        Double discount = saved.getDiscount() > 0 ? saved.getDiscount() : 0.0;

       
        if (ledger == null) {
            double adjustedTotalDue = Math.max(0.0, planTotal - discount);

            ledger = LedgerEntry.builder()
                    .student(student)
                    .totalDue(adjustedTotalDue)
                    .totalPaid(saved.getAmountPaid())
                    .balance(Math.max(0.0, adjustedTotalDue - saved.getAmountPaid()))
                    .lastPaymentDate(LocalDate.now())
                    .build();
        } else {
      
            if (discount > 0) {
                ledger.setTotalDue(Math.max(0.0, ledger.getTotalDue() - discount));
            }

        
            ledger.setTotalPaid(ledger.getTotalPaid() + saved.getAmountPaid());

           
            ledger.setBalance(Math.max(0.0, ledger.getTotalDue() - ledger.getTotalPaid()));

            ledger.setLastPaymentDate(LocalDate.now());
        }

        ledgerRepository.save(ledger);
        return saved;
    }

    @Override
    @Transactional
    public Payment makePayment(PaymentRequest request) {

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found: " + request.getStudentId()
                ));

        FeePlan feePlan = feePlanRepository.findById(request.getFeePlanId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "FeePlan not found: " + request.getFeePlanId()
                ));

        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setFeePlan(feePlan);
        payment.setAmountPaid(request.getAmountPaid());
        payment.setDiscount(request.getDiscount()); 
        payment.setLateFee(0.0);
        payment.setPaymentDate(LocalDate.now());

        return makePayment(payment);
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
