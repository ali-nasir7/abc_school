package com.backend.Abroad_School.dto;

import com.backend.Abroad_School.model.Staff;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffDTO {

    private Long id;

    @NotBlank
    private String fullName;

    @NotBlank
    private String cnic;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String contactNumber;
    private String email;
    private String address;
    @NotNull
    private Staff.Designation designation;
    private Long salaryStructureId;
}
