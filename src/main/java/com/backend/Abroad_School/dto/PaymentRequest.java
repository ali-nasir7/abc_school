package com.backend.Abroad_School.dto;

import lombok.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    @NotNull
    private Long studentId;

    @NotNull
    private Long feePlanId;

    @Min(0)
    private double amountPaid;

    @Min(0)
    private double discount = 0;
}
