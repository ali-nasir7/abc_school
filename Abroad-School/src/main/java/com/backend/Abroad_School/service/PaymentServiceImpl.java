package com.backend.Abroad_School.service;


import com.backend.Abroad_School.model.*;
import com.backend.Abroad_School.repository.LedgerRepository;
import com.backend.Abroad_School.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final LedgerRepository ledgerRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, LedgerRepository ledgerRepository) {
        this.paymentRepository = paymentRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Override
    public Payment makePayment(Payment payment) {
        payment.setPaymentDate(LocalDate.now());
        Payment saved = paymentRepository.save(payment);

        // Update Ledger
        LedgerEntry ledger = ledgerRepository.findByStudent(payment.getStudent());
        if (ledger == null) {
            ledger = LedgerEntry.builder()
                    .student(payment.getStudent())
                    .totalPaid(payment.getAmountPaid())
                    .balance(payment.getFeePlan().getFeeHeads().stream().mapToDouble(FeeHead::getAmount).sum() - payment.getAmountPaid())
                    .totalDue(payment.getFeePlan().getFeeHeads().stream().mapToDouble(FeeHead::getAmount).sum())
                    .lastPaymentDate(LocalDate.now())
                    .build();
        } else {
            ledger.setTotalPaid(ledger.getTotalPaid() + payment.getAmountPaid());
            ledger.setBalance(ledger.getBalance() - payment.getAmountPaid());
            ledger.setLastPaymentDate(LocalDate.now());
        }
        ledgerRepository.save(ledger);

        return saved;
    }

    @Override
    public List<Payment> getStudentPayments(Student student) {
        return paymentRepository.findByStudent(student);
    }
}
