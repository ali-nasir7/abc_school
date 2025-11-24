package com.backend.Abroad_School.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Voucher belongs to student
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private double totalAmount;
    private int lateFee;
    private LocalDate dueDate;
    private boolean paid = false;
    private boolean lateFeeApplied = false; 
    private double discount;

    private LocalDate paymentDate;
    private LocalDate createdAt = LocalDate.now();

    // Store PDF as bytes
    @Lob
    @Column(name = "voucher_pdf")
    private byte[] pdfFile;
}
