package com.backend.Abroad_School.dto;

import lombok.*;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryStructureDTO {
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal basicPay;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal allowances;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal deductions;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal tax;
}
