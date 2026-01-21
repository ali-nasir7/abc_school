package com.backend.Abroad_School.dto;

import java.time.LocalDate;

public class VoucherDTO {

    private Long id;
    private Double discount;
    private LocalDate dueDate;
    private Double lateFee;
    private Boolean paid;
    private Long studentId; // only the student ID
    private Double totalAmount;

    public VoucherDTO() {}

    public VoucherDTO(Long id, Double discount, LocalDate dueDate, Double lateFee, Boolean paid, Long studentId, Double totalAmount) {
        this.id = id;
        this.discount = discount;
        this.dueDate = dueDate;
        this.lateFee = lateFee;
        this.paid = paid;
        this.studentId = studentId;
        this.totalAmount = totalAmount;
    }

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Double getLateFee() { return lateFee; }
    public void setLateFee(Double lateFee) { this.lateFee = lateFee; }

    public Boolean getPaid() { return paid; }
    public void setPaid(Boolean paid) { this.paid = paid; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
}
