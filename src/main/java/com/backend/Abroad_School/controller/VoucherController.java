package com.backend.Abroad_School.controller;

import com.backend.Abroad_School.dto.VoucherDTO;
import com.backend.Abroad_School.dto.VoucherRequest;
import com.backend.Abroad_School.model.Voucher;
import com.backend.Abroad_School.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/unpaid")
    public ResponseEntity<List<VoucherDTO>> listUnpaidVouchers() {
        List<VoucherDTO> dtos = voucherService
                .mapListToDTO(voucherService.getAllUnpaidVouchers());
        return ResponseEntity.ok(dtos);
    }

 
    @PostMapping("/apply-late-fees")
    public ResponseEntity<String> applyLateFeesNow() {
        voucherService.applyLateFeesForAllPendingStudents();
        return ResponseEntity.ok("Late fees applied (where applicable).");
    }

    @PostMapping("/")
    public ResponseEntity<VoucherDTO> createVoucher(@RequestBody VoucherRequest request) {
        Voucher saved = voucherService.createVoucher(request);
        return ResponseEntity.ok(voucherService.mapToDTO(saved));
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<VoucherDTO> markPaid(@PathVariable Long id) {
        Voucher v = voucherService.markVoucherPaid(id);
        return ResponseEntity.ok(voucherService.mapToDTO(v));
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
