package com.backend.Abroad_School.service;

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


@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private static final Logger logger = LoggerFactory.getLogger(VoucherServiceImpl.class);

    private final VoucherRepository voucherRepository;
    private final PDFService pdfService;
    private final LedgerRepository ledgerRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    private static final int DEFAULT_LATE_FEE = 500;

    @Override
    public List<Voucher> getAllUnpaidVouchers() {
        return voucherRepository.findAllUnpaidVouchers();
    }

    @Override
    public List<Voucher> getUnpaidVouchersByStudent(Long studentId) {
        if (studentId == null) {
            // Defensive: if null, return empty list (scheduler should call getAllUnpaidVouchers instead)
            return Collections.emptyList();
        }
        return voucherRepository.findUnpaidVouchersByStudent(studentId);
    }

    @Override
    @Transactional
    public void applyLateFeesForAllPendingStudents() {
        List<Voucher> pending = voucherRepository.findAllUnpaidVouchers();
        LocalDate today = LocalDate.now();

        for (Voucher v : pending) {
            try {
                if (v == null) continue;
                if (v.getDueDate() == null || !today.isAfter(v.getDueDate())) continue;
                if (v.isLateFeeApplied()) continue;

                // get late fee amount either from voucher (if set) or default
                int lateFee = (v.getLateFee() > 0) ? v.getLateFee() : DEFAULT_LATE_FEE;

                v.setLateFee(lateFee);
                v.setTotalAmount(v.getTotalAmount() + lateFee);
                v.setLateFeeApplied(true);

                // Ledger update
                Student st = v.getStudent();
                if (st != null) {
                    LedgerEntry ledger = ledgerRepository.findByStudent(st);
                    if (ledger == null) {
                        ledger = LedgerEntry.builder()
                                .student(st)
                                .totalDue(v.getTotalAmount())
                                .totalPaid(0.0)
                                .balance(v.getTotalAmount())
                                .lastPaymentDate(null)
                                .build();
                    } else {
                        ledger.setTotalDue(ledger.getTotalDue() + lateFee);
                        ledger.setBalance(ledger.getBalance() + lateFee);
                    }
                    ledgerRepository.save(ledger);
                }

                // Regenerate PDF if student present
                if (st != null && st.getId() != null) {
                    try {
                        byte[] pdf = pdfService.generateAdmissionVoucherPDF(st.getId());
                        v.setPdfFile(pdf);
                    } catch (Exception e) {
                        logger.error("Failed to regenerate PDF for voucher {} (student {}): {}", v.getId(), (st != null ? st.getId() : null), e.getMessage(), e);
                    }
                }

                // Persist voucher update
                voucherRepository.save(v);

                // Notify parent (voucher-level)
                try {
                    notificationService.sendLateFeeNotificationToParent(v);
                } catch (Exception e) {
                    logger.error("Failed to send late fee notification for voucher {}: {}", v.getId(), e.getMessage(), e);
                }
            } catch (Exception ex) {
                // Keep processing other vouchers even if one fails
                logger.error("Error processing pending voucher id {}: {}", v != null ? v.getId() : null, ex.getMessage(), ex);
            }
        }
    }

    @Override
    @Transactional
    public Voucher createVoucher(Voucher voucher) {
        if (voucher.getStudent() == null) {
            throw new RuntimeException("Voucher must be associated with a Student");
        }

        if (voucher.getDueDate() == null) {
            voucher.setDueDate(LocalDate.now().plusDays(10));
        }

        voucher.setCreatedAt(LocalDate.now());
        voucher.setPaid(false);

        Student s = voucher.getStudent();
        FeePlan plan = s.getFeePlan();
        if (plan != null) {
            double totalAmount = plan.getFeeHeads().stream().mapToDouble(FeeHead::getAmount).sum();
            if (voucher.getDiscount() > 0) totalAmount -= voucher.getDiscount();
            voucher.setTotalAmount(totalAmount);
        }

        Voucher saved = voucherRepository.save(voucher);

        // ledger update
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
        return pdfService.generateAdmissionVoucherPDF(voucher.getStudent().getId());
    }

    @Override
    public void saveVoucher(Voucher voucher) {
        voucherRepository.save(voucher);
    }
}
