package com.backend.Abroad_School.dto;

import lombok.*;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRequestDTO {
    @NotNull
    private Long staffId;

    @NotNull
    private LocalDate periodStart;

    @NotNull
    private LocalDate periodEnd;

    private String remarks;
}
