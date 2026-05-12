package com.backend.Abroad_School.controller;

import com.backend.Abroad_School.scheduler.FeeReminderScheduler;
import com.backend.Abroad_School.service.WhatsAppService;
import com.backend.Abroad_School.service.WhatsAppService.WhatsAppResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
@Slf4j
public class WhatsAppController {

    private final WhatsAppService whatsAppService;
    private final FeeReminderScheduler feeReminderScheduler;

    // ── Template test — apna number de ke confirm karo ───────────────────────
    @PostMapping("/test-template")
    public ResponseEntity<String> testTemplate(
            @RequestParam String number,
            @RequestParam(defaultValue = "Parent") String parentName,
            @RequestParam(defaultValue = "12 May") String issueDate,
            @RequestParam(defaultValue = "Test Child") String childName) {

        WhatsAppResult result = whatsAppService.sendFeeReminderTemplate(
                number, parentName, issueDate, childName);

        return result.success()
                ? ResponseEntity.ok("Template sent: " + result.response())
                : ResponseEntity.badRequest().body("Failed: " + result.response());
    }

    // ── Manual scheduler trigger — SIRF DEV/TEST ─────────────────────────────
    // PRODUCTION PE DEPLOY SE PEHLE YE ENDPOINT COMMENT OUT KARO
    @PostMapping("/trigger-scheduler")
    public ResponseEntity<String> triggerScheduler() {
        try {
            log.warn("Manual scheduler trigger called via API");
            feeReminderScheduler.processReminders();
            return ResponseEntity.ok("Scheduler triggered successfully");
        } catch (Exception e) {
            log.error("Manual scheduler trigger failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed: " + e.getMessage());
        }
    }
}