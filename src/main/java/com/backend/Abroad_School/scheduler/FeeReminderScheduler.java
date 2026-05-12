package com.backend.Abroad_School.scheduler;

import com.backend.Abroad_School.model.Student;
import com.backend.Abroad_School.model.Voucher;
import com.backend.Abroad_School.model.WhatsAppBlockedNumber;
import com.backend.Abroad_School.repository.VoucherRepository;
import com.backend.Abroad_School.repository.WhatsAppBlockedNumberRepository;
import com.backend.Abroad_School.service.VoucherService;
import com.backend.Abroad_School.service.WhatsAppService;
import com.backend.Abroad_School.service.WhatsAppService.WhatsAppResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeeReminderScheduler {

    private final VoucherRepository voucherRepository;
    private final VoucherService voucherService;
    private final WhatsAppService whatsAppService;
    private final WhatsAppBlockedNumberRepository blockedRepo;

    @Value("${whatsapp.max-sends-per-run:50}")
    private int maxSendsPerRun;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    // Template issue date format — template mein {{2}} ke liye
    private static final DateTimeFormatter ISSUE_FMT =
            DateTimeFormatter.ofPattern("d MMM");

    // Meta definitive bad number codes — blacklist karo
    private static final Set<String> DEFINITIVE_BAD_NUMBER_CODES = Set.of(
            "131030",  // Not on WhatsApp
            "131047",  // Invalid number
            "131026",  // Undeliverable
            "100"      // Invalid parameter
    );

    // Transient — blacklist mat karo, kal retry hogi automatically
    private static final Set<String> TRANSIENT_CODES = Set.of(
            "META_SERVER_ERROR",
            "NETWORK_TIMEOUT",
            "MEDIA_UPLOAD_FAILED"
    );

    // ── 9 PM daily ───────────────────────────────────────────────────────────
    @Scheduled(cron = "0 0 21 * * *")
    public void processReminders() {
        LocalDate today = LocalDate.now();
        int sendCount = 0;

        log.info("=== FeeReminderScheduler START: {} ===", today);

        // ── LOOP 1: Unsent vouchers ───────────────────────────────────────────
        // PDF bhejo + template msg saath mein
        // Late fee wale naye vouchers bhi yahan aa jayenge (voucherSent=false hoga)
        List<Voucher> unsentList = voucherRepository.findUnsentVouchers();
        log.info("Unsent vouchers: {}", unsentList.size());

        for (Voucher voucher : unsentList) {
            if (sendCount >= maxSendsPerRun) {
                log.warn("Max limit ({}) hit — PDF loop stopped", maxSendsPerRun);
                break;
            }
            try {
                String contact = resolveContact(voucher.getStudent());
                if (contact == null) continue;

                // PDF bhejo
                boolean pdfSent = sendVoucherPdf(voucher, contact);

                // Template msg bhejo — chahe PDF success ho ya fail
                // Kyunki template alag channel hai, PDF alag
                boolean templateSent = sendVoucherTemplate(voucher, contact);

                // Agar dono mein se koi bhi success hua toh count badhao
                if (pdfSent || templateSent) sendCount++;

            } catch (Exception e) {
                log.error("Error in PDF loop for voucher {}: {}",
                        voucher.getId(), e.getMessage());
            }
        }

        // ── LOOP 2: Reminder — 1-10 din pehle ────────────────────────────────
        // Sirf unpaid + voucherSent=true + reminderSent=false
        List<Voucher> reminderList = voucherRepository.findVouchersNeedingReminder();
        log.info("Vouchers needing reminder: {}", reminderList.size());

        for (Voucher voucher : reminderList) {
            if (sendCount >= maxSendsPerRun) {
                log.warn("Max limit ({}) hit — reminder loop stopped", maxSendsPerRun);
                break;
            }
            try {
                long daysLeft = ChronoUnit.DAYS.between(today, voucher.getDueDate());

                // 0 ya negative = overdue = skip (reminder ka koi faida nahi)
                // 11+ = abhi door hai = skip
                if (daysLeft < 1 || daysLeft > 10) continue;

                String contact = resolveContact(voucher.getStudent());
                if (contact == null) continue;

                boolean sent = sendReminder(voucher, contact);
                if (sent) sendCount++;

            } catch (Exception e) {
                log.error("Error in reminder loop for voucher {}: {}",
                        voucher.getId(), e.getMessage());
            }
        }

        log.info("=== FeeReminderScheduler END — Total sent: {} ===", sendCount);
    }

    // ── PDF Send ──────────────────────────────────────────────────────────────
    @Transactional
    protected boolean sendVoucherPdf(Voucher voucher, String contact) {
        byte[] pdf;
        try {
            // Cached PDF use karo, warna generate karo
            pdf = (voucher.getPdfFile() != null && voucher.getPdfFile().length > 0)
                    ? voucher.getPdfFile()
                    : voucherService.generateVoucherPDF(voucher.getId());
        } catch (Exception e) {
            // PDF generation server issue — number blacklist nahi, kal retry
            log.error("PDF generation failed for voucher {} (not blacklisting): {}",
                    voucher.getId(), e.getMessage());
            return false;
        }

        Student student = voucher.getStudent();
        String filename = "voucher_" + student.getGrNumber() + ".pdf";
        String caption = "Fee voucher for " + student.getFullName()
                + ". Due: " + voucher.getDueDate().format(FMT)
                + ". Amount: Rs. " + String.format("%.0f", voucher.getTotalAmount())
                + " — Abroad Schooling System.";

        WhatsAppResult result = whatsAppService.sendPdfVoucher(
                contact, pdf, filename, caption);

        if (result.success()) {
            voucher.setVoucherSent(true);
            voucherRepository.save(voucher);
            log.info("PDF sent OK → {} ({})", student.getFullName(), contact);
            return true;
        }

        handleFailure(contact, result, "PDF");
        return false;
    }

    // ── Template msg with voucher info ────────────────────────────────────────
    // Ye PDF ke saath bhi jaata hai (Loop 1)
    // Template: Dear {{1}}, reminder for voucher issued on {{2}} for child {{3}}
    @Transactional
    protected boolean sendVoucherTemplate(Voucher voucher, String contact) {
        Student student = voucher.getStudent();

        // {{1}} = "Parent" (parent ka naam DB mein nahi hai, generic use karo)
        // {{2}} = voucher issue/created date
        // {{3}} = student full name
        String parentName = "Parent";
        String issueDate  = voucher.getCreatedAt() != null
                ? voucher.getCreatedAt().format(ISSUE_FMT)
                : voucher.getDueDate().format(ISSUE_FMT); // fallback
        String childName  = student.getFullName();

        WhatsAppResult result = whatsAppService.sendFeeReminderTemplate(
                contact, parentName, issueDate, childName);

        if (result.success()) {
            log.info("Voucher template sent OK → {} ({})",
                    student.getFullName(), contact);
            return true;
        }

        // Template fail — same blacklist logic
        handleFailure(contact, result, "VOUCHER_TEMPLATE");
        return false;
    }

    // ── Reminder template — 1-10 din pehle ───────────────────────────────────
    @Transactional
    protected boolean sendReminder(Voucher voucher, String contact) {
        Student student = voucher.getStudent();

        String parentName = "Parent";
        String issueDate  = voucher.getCreatedAt() != null
                ? voucher.getCreatedAt().format(ISSUE_FMT)
                : voucher.getDueDate().format(ISSUE_FMT);
        String childName  = student.getFullName();

        WhatsAppResult result = whatsAppService.sendFeeReminderTemplate(
                contact, parentName, issueDate, childName);

        if (result.success()) {
            voucher.setReminderSent(true);
            voucherRepository.save(voucher);
            log.info("Reminder sent OK → {} ({})", student.getFullName(), contact);
            return true;
        }

        handleFailure(contact, result, "REMINDER");
        return false;
    }

    // ── Smart failure handler ─────────────────────────────────────────────────
    private void handleFailure(String contact, WhatsAppResult result, String type) {
        String code = result.errorCode();

        if (TRANSIENT_CODES.contains(code)) {
            log.warn("[{}] Transient error {} → will retry tomorrow: {}",
                    type, contact, code);
            return;
        }

        if (DEFINITIVE_BAD_NUMBER_CODES.contains(code)) {
            if (!blockedRepo.existsByPhoneNumber(contact)) {
                blockedRepo.save(WhatsAppBlockedNumber.builder()
                        .phoneNumber(contact)
                        .reason("Meta code: " + code + " | " + result.response())
                        .build());
                log.warn("[{}] Blacklisted: {} (code: {})", type, contact, code);
            }
            return;
        }

        log.error("[{}] Unknown error {} (code: {}): {}",
                type, contact, code, result.response());
    }

    // ── Contact resolver: contact1 → contact2 fallback ───────────────────────
    private String resolveContact(Student student) {
        if (isValidRaw(student.getParentContact1())) {
            String fmt = formatNumber(student.getParentContact1());
            if (fmt != null && !blockedRepo.existsByPhoneNumber(fmt)) return fmt;
            log.debug("Contact1 invalid/blacklisted for {}", student.getGrNumber());
        }

        if (isValidRaw(student.getParentContact2())) {
            String fmt = formatNumber(student.getParentContact2());
            if (fmt != null && !blockedRepo.existsByPhoneNumber(fmt)) return fmt;
        }

        log.warn("No valid contact for student: {}", student.getGrNumber());
        return null;
    }

    // ── 03XX → 923XX ─────────────────────────────────────────────────────────
    private String formatNumber(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;

        if (digits.startsWith("92") && digits.length() == 12) return digits;
        if (digits.startsWith("0")  && digits.length() == 11)
            return "92" + digits.substring(1);
        if (digits.startsWith("3")  && digits.length() == 10)
            return "92" + digits;

        log.warn("Unrecognized number format: '{}'", raw);
        return null;
    }

    private boolean isValidRaw(String raw) {
        return raw != null && !raw.isBlank();
    }
}