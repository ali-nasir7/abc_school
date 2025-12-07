package com.backend.Abroad_School.service;

import com.backend.Abroad_School.model.*;
import com.backend.Abroad_School.repository.*;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LateFeeScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LateFeeScheduler.class);

    private final StudentRepository studentRepository;
    private final LedgerRepository ledgerRepository;
    private final LateFeeChargeRepository lateFeeChargeRepository;
    private final PaymentRepository paymentRepository;
    private final VoucherService voucherService;
    private final NotificationService notificationService;
    private final VoucherRepository voucherRepository;

    @Value("${school.latefee.amount:500}")
    private double LATE_FEE_AMOUNT;

    @Scheduled(cron = "0 0 2 * * ?") // daily at 2 AM
    @Transactional
    public void applyMonthlyLateFee() {
        LocalDate today = LocalDate.now();

        // Only after 10th of the month
        if (today.getDayOfMonth() <= 10) return;

        YearMonth currentMonth = YearMonth.from(today);
        String monthYear = currentMonth.toString();

        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            LedgerEntry ledger = ledgerRepository.findByStudent(student);
            if (ledger == null || ledger.getBalance() <= 0) continue;

            boolean alreadyCharged = lateFeeChargeRepository
                    .findByStudentAndMonthYear(student, monthYear)
                    .isPresent();
            if (alreadyCharged) continue;

            // Create Payment record for late fee
            Payment lateFeePayment = new Payment();
            lateFeePayment.setStudent(student);
            lateFeePayment.setFeePlan(null);
            lateFeePayment.setAmountPaid(LATE_FEE_AMOUNT);
            lateFeePayment.setDiscount(0.0);
            lateFeePayment.setLateFee(LATE_FEE_AMOUNT);
            lateFeePayment.setPaymentDate(today);
            paymentRepository.save(lateFeePayment);

            // Update Ledger
            ledger.setTotalPaid(ledger.getTotalPaid() + LATE_FEE_AMOUNT);
            ledger.setBalance(Math.max(0.0, ledger.getTotalDue() - ledger.getTotalPaid()));
            ledger.setLastPaymentDate(today);
            ledgerRepository.save(ledger);

            // Record LateFeeCharge to prevent duplicates
            LateFeeCharge charge = LateFeeCharge.builder()
                    .student(student)
                    .monthYear(monthYear)
                    .build();
            lateFeeChargeRepository.save(charge);

            // Handle unpaid vouchers
            List<Voucher> unpaidVouchers = voucherRepository.findUnpaidVouchersByStudent(student.getId());
            for (Voucher voucher : unpaidVouchers) {
                try {
                    byte[] pdf = voucherService.generateVoucherPDF(voucher.getId());
                    voucher.setPdfFile(pdf);
                    voucherService.saveVoucher(voucher);
                } catch (Exception e) {
                    logger.error("Error generating PDF for voucher {}", voucher.getId(), e);
                }

                // Notify parent
                try {
                    notificationService.sendLateFeeNotificationToParent(voucher);
                } catch (Exception e) {
                    logger.error("Error sending late fee notification for voucher {}", voucher.getId(), e);
                }
            }
        }

        logger.info("Monthly late fee scheduler executed successfully for month {}", monthYear);
    }
}
