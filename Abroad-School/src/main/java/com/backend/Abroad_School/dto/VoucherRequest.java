package com.backend.Abroad_School.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class VoucherRequest {
    private Long studentId;     // required
    private Double discount;    // optional, default 0.0
    private LocalDate dueDate;  // optional
    private Double lateFee;     // optional, default 0.0
}
