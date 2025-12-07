package com.backend.Abroad_School.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "salary_structure")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Ali Nasir - Grade 5

    @Builder.Default
    private BigDecimal basicPay = BigDecimal.ZERO;

   // percentage men accept nhi krega
    @Builder.Default
    private BigDecimal allowances = BigDecimal.ZERO; 
    @Builder.Default
    private BigDecimal deductions = BigDecimal.ZERO; 
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO; 

    private boolean active = true;
}
