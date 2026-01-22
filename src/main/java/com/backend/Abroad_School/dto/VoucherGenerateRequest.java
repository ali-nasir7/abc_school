package com.backend.Abroad_School.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class VoucherGenerateRequest {
    private Long studentId;
    private Integer month; // 1-12
    private Integer year;
    private Double discount; // optional
    private LocalDate dueDate; // optional
}
