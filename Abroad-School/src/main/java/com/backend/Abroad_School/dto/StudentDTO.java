package com.backend.Abroad_School.dto;

import com.backend.Abroad_School.model.StudentStatus;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {

    private Long id;                 
    @NotBlank
    private String fullName;
    private String fatherName;
    private String motherName;
    @NotBlank(message = "CNIC required")
    @Pattern(
        regexp = "^[0-9]{5}-[0-9]{7}-[0-9]$",
        message = "CNIC must be 12345-1234567-1"
    )
    private String fatherCnic;

    @NotBlank(message = "CNIC required")
    @Pattern(
        regexp = "^[0-9]{5}-[0-9]{7}-[0-9]$",
        message = "CNIC must be 12345-1234567-1"
    )
    private String motherCnic;
    private LocalDate dateOfBirth;
    private String className;
    private String section;
    private String groupName;
    private LocalDate admissionDate;
    private String previousSchool;
    @NotBlank(message = "CNIC required")
    @Pattern(
        regexp = "^[0-9]{5}-[0-9]{7}-[0-9]$",
        message = "CNIC must be 12345-1234567-1"
    )
    private String parentContact1;
    private String parentContact2;
    private String address;
    private String bFormNumber;
    private String grNumber;        
    private Integer rollNumber;      
    private StudentStatus studentStatus;
    private Long feePlanId;
}
