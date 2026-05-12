package com.backend.Abroad_School.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "vouchers", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_student_month_year",
        columnNames = {"student_id", "voucher_month", "voucher_year"}
    )
})
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
    private double lateFee;
    private LocalDate dueDate;
    private boolean paid = false;
    private boolean lateFeeApplied = false; 
    private double discount;

    private LocalDate paymentDate;
    private LocalDate createdAt = LocalDate.now();

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
private boolean voucherSent = false;

@Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
private boolean reminderSent = false;

@Column(nullable = false)
private Integer voucherMonth;

@Column(nullable = false)
private Integer voucherYear;

    // Store PDF as bytes
    @Lob
    @Column(name = "voucher_pdf", columnDefinition = "LONGBLOB")
    private byte[] pdfFile;
}
