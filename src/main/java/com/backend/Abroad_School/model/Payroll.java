package com.backend.Abroad_School.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payroll")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Staff staff;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal grossAmount;
    private BigDecimal totalAllowances;
    private BigDecimal totalDeductions;
    private BigDecimal tax;
    private BigDecimal netPay;
    private LocalDate processedAt;
    private String remarks;
}
