package com.backend.Abroad_School.controller;

import com.backend.Abroad_School.model.Voucher;
import com.backend.Abroad_School.service.VoucherService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping("/unpaid")
    public ResponseEntity<List<Voucher>> listUnpaidVouchers() {
        return ResponseEntity.ok(voucherService.getAllUnpaidVouchers());
    }

    @PostMapping("/apply-late-fees")
    public ResponseEntity<String> applyLateFeesNow() {
        voucherService.applyLateFeesForAllPendingStudents();
        return ResponseEntity.ok("Late fees applied (where applicable).");
    }

    @PostMapping("/")
    public ResponseEntity<Voucher> createVoucher(@RequestBody Voucher voucher) {
        Voucher saved = voucherService.createVoucher(voucher);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<Voucher> markPaid(@PathVariable Long id) {
        Voucher v = voucherService.markVoucherPaid(id);
        return ResponseEntity.ok(v);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getVoucherPdf(@PathVariable Long id) {
        byte[] pdf = voucherService.generateVoucherPDF(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=voucher_" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
