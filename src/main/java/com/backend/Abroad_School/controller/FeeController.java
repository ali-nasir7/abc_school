package com.backend.Abroad_School.controller;

import com.backend.Abroad_School.dto.PaymentRequest;
import com.backend.Abroad_School.model.FeeHead;
import com.backend.Abroad_School.model.Payment;
import com.backend.Abroad_School.service.FeeHeadService;
import com.backend.Abroad_School.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fees")
public class FeeController {

    private final FeeHeadService feeHeadService;
    private final PaymentService paymentService;
    

    public FeeController(FeeHeadService feeHeadService,
                         PaymentService paymentService) {
        this.feeHeadService = feeHeadService;
        this.paymentService = paymentService;
        
    }

    
    @PostMapping("/head")
    public FeeHead createFeeHead(@RequestBody FeeHead feeHead) {
        return feeHeadService.createFeeHead(feeHead);
    }

    @GetMapping("/head")
    public List<FeeHead> getAllFeeHeads() {
        return feeHeadService.getAllFeeHeads();
    }

    @PutMapping("/head/{id}")
    public FeeHead updateFeeHead(@PathVariable Long id, @RequestBody FeeHead feeHead) {
        return feeHeadService.updateFeeHead(id, feeHead);
    }

    @DeleteMapping("/head/{id}")
    public void deleteFeeHead(@PathVariable Long id) {
        feeHeadService.deleteFeeHead(id);
    }

    @PostMapping("/payment")
    public ResponseEntity<Payment> makePayment(@RequestBody PaymentRequest request) {
        Payment payment = paymentService.makePayment(request);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/payment/student/{studentId}")
    public ResponseEntity<List<Payment>> getStudentPayments(@PathVariable Long studentId) {
        List<Payment> payments = paymentService.getPaymentsForStudent(studentId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/payment/mark-paid")
    public ResponseEntity<String> markPaid(@RequestParam Long voucherId) {
       
        return ResponseEntity.ok("Use /api/vouchers/{id}/mark-paid to mark voucher paid.");
    }
}
