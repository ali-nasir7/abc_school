package com.backend.Abroad_School.service;

import com.backend.Abroad_School.dto.VoucherDTO;
import com.backend.Abroad_School.dto.VoucherRequest;
import com.backend.Abroad_School.model.*;
import com.backend.Abroad_School.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private static final Logger logger = LoggerFactory.getLogger(VoucherServiceImpl.class);

    private final VoucherRepository voucherRepository;
    private final PDFService pdfService;
    private final LedgerRepository ledgerRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final StudentRepository studentRepository;

    private static final double DEFAULT_LATE_FEE = 500.0;

    @Override
    public List<Voucher> getAllUnpaidVouchers() {
        return voucherRepository.findAllUnpaidVouchers();
    }

    @Override
    public List<Voucher> getUnpaidVouchersByStudent(Long studentId) {
        if (studentId == null) return Collections.emptyList();
        return voucherRepository.findUnpaidVouchersByStudent(studentId);
    }

    @Override
    @Transactional
    public void applyLateFeesForAllPendingStudents() {
        List<Voucher> pending = voucherRepository.findAllUnpaidVouchers();
        LocalDate today = LocalDate.now();

        for (Voucher v : pending) {
            try {
                if (v == null || v.getDueDate() == null) continue;
                if (!today.isAfter(v.getDueDate()) || v.isLateFeeApplied()) continue;

                double lateFee = v.getLateFee() > 0 ? v.getLateFee() : DEFAULT_LATE_FEE;
                v.setLateFee(lateFee);
                v.setTotalAmount(v.getTotalAmount() + lateFee);
                v.setLateFeeApplied(true);

                Student st = v.getStudent();
                if (st != null) {
                    LedgerEntry ledger = ledgerRepository.findByStudent(st);
                    if (ledger != null) {
                        ledger.setBalance(ledger.getBalance() + lateFee);
                        ledger.setTotalDue(ledger.getTotalDue() + lateFee);
                        ledgerRepository.save(ledger);
                    }
                }

                try {
                    byte[] pdf = pdfService.generateVoucherPDF(v);
                    v.setPdfFile(pdf);
                } catch (Exception e) {
                    logger.error("Failed to regenerate PDF for voucher {}: {}", v.getId(), e.getMessage(), e);
                }

                voucherRepository.save(v);

             
                try {
                    notificationService.sendLateFeeNotificationToParent(v);
                } catch (Exception e) {
                    logger.error("Failed to send late fee notification for voucher {}: {}", v.getId(), e.getMessage(), e);
                }

            } catch (Exception ex) {
                logger.error("Error processing pending voucher id {}: {}", v != null ? v.getId() : null, ex.getMessage(), ex);
            }
        }
    }

    @Override
    @Transactional
    public Voucher createVoucher(VoucherRequest request) {
        Student s = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found: " + request.getStudentId()));

        Voucher voucher = Voucher.builder()
                .student(s)
                .discount(request.getDiscount() != null ? request.getDiscount() : 0.0)
                .lateFee(request.getLateFee() != null ? request.getLateFee() : 0.0)
                .dueDate(request.getDueDate() != null ? request.getDueDate() : LocalDate.now().plusDays(10))
                .paid(false)
                .lateFeeApplied(false)
                .createdAt(LocalDate.now())
                .build();

      
        FeePlan plan = s.getFeePlan();
        double total = plan != null && plan.getFeeHeads() != null ?
                plan.getFeeHeads().stream().mapToDouble(FeeHead::getAmount).sum() : 0.0;

        double discount = Math.max(0.0, voucher.getDiscount());
        total = Math.max(0.0, total - discount);
        voucher.setTotalAmount(total);

        Voucher saved = voucherRepository.save(voucher);

   
        LedgerEntry ledger = ledgerRepository.findByStudent(s);
        if (ledger == null) {
            ledger = LedgerEntry.builder()
                    .student(s)
                    .totalDue(saved.getTotalAmount())
                    .totalPaid(0.0)
                    .balance(saved.getTotalAmount())
                    .lastPaymentDate(null)
                    .build();
        } else {
            ledger.setTotalDue(ledger.getTotalDue() + saved.getTotalAmount());
            ledger.setBalance(ledger.getBalance() + saved.getTotalAmount());
        }
        ledgerRepository.save(ledger);

  
        try {
            byte[] pdf = pdfService.generateVoucherPDF(saved);
            saved.setPdfFile(pdf);
            voucherRepository.save(saved);
        } catch (Exception e) {
            logger.error("Failed to generate PDF for voucher {}: {}", saved.getId(), e.getMessage(), e);
        }

        return saved;
    }

    @Override
    @Transactional
    public Voucher markVoucherPaid(Long voucherId) {
        Voucher v = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher not found: " + voucherId));
        if (v.isPaid()) return v;

        v.setPaid(true);
        v.setPaymentDate(LocalDate.now());

        Payment p = new Payment();
        Student student = v.getStudent();
        p.setStudent(student);
        p.setFeePlan(student != null ? student.getFeePlan() : null);
        p.setAmountPaid(v.getTotalAmount());
        p.setDiscount(v.getDiscount());
        p.setLateFee(v.getLateFee());
        p.setPaymentDate(LocalDate.now());
        paymentRepository.save(p);

        if (student != null) {
            LedgerEntry ledger = ledgerRepository.findByStudent(student);
            if (ledger == null) {
                ledger = LedgerEntry.builder()
                        .student(student)
                        .totalDue(0.0)
                        .totalPaid(p.getAmountPaid())
                        .balance(0.0)
                        .lastPaymentDate(LocalDate.now())
                        .build();
            } else {
                ledger.setTotalPaid(ledger.getTotalPaid() + p.getAmountPaid());
                ledger.setBalance(Math.max(0.0, ledger.getTotalDue() - ledger.getTotalPaid()));
                ledger.setLastPaymentDate(LocalDate.now());
            }
            ledgerRepository.save(ledger);
        }

        voucherRepository.save(v);

        try {
            notificationService.sendPaymentReceivedNotificationToParent(v);
        } catch (Exception e) {
            logger.error("Failed to send payment-received notification for voucher {}: {}", v.getId(), e.getMessage(), e);
        }

        return v;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateVoucherPDF(Long voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher not found: " + voucherId));
        if (voucher.getStudent() == null) throw new RuntimeException("Voucher has no student associated");
        return pdfService.generateVoucherPDF(voucher);
    }

    @Override
    public void saveVoucher(Voucher voucher) {
        voucherRepository.save(voucher);
    }

    @Override
    public VoucherDTO mapToDTO(Voucher voucher) {
        return new VoucherDTO(
                voucher.getId(),
                voucher.getDiscount(),
                voucher.getDueDate(),
                voucher.getLateFee(),
                voucher.isPaid(),
                voucher.getStudent().getId(),
                voucher.getTotalAmount()
        );
    }

    @Override
    public List<VoucherDTO> mapListToDTO(List<Voucher> vouchers) {
        return vouchers.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
}
