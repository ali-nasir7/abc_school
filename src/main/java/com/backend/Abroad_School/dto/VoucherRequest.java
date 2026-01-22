package com.backend.Abroad_School.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class VoucherRequest {
    private Long studentId;     
    private Double discount;   
    private LocalDate dueDate;  
    private Double lateFee;     
}
