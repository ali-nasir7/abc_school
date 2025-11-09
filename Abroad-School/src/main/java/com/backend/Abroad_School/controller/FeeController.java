package com.backend.Abroad_School.controller;

import com.backend.Abroad_School.model.FeeHead;
import com.backend.Abroad_School.model.Payment;
import com.backend.Abroad_School.model.Student;
import com.backend.Abroad_School.service.FeeHeadService;
import com.backend.Abroad_School.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fees")
public class FeeController {

    private final FeeHeadService feeHeadService;
    private final PaymentService paymentService;

    public FeeController(FeeHeadService feeHeadService, PaymentService paymentService) {
        this.feeHeadService = feeHeadService;
        this.paymentService = paymentService;
    }

    // Fee Endpoints
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

    // Payment Endpoint
    @PostMapping("/payment")
    public Payment makePayment(@RequestBody Payment payment) {
        return paymentService.makePayment(payment);
    }

    @GetMapping("/payment/student/{studentId}")
    public List<Payment> getStudentPayments(@PathVariable Long studentId) {
        Student student = new Student();
        student.setId(studentId);
        return paymentService.getStudentPayments(student);
    }
}
