package com.backend.Abroad_School.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String cnic;
    private LocalDate dateOfBirth;
    private String contactNumber;
    private String email;
    private String address;

    @Enumerated(EnumType.STRING)
    private Designation designation;

    @ManyToOne
    private SalaryStructure salaryStructure;

    private boolean active = true;

    public enum Designation {
        TEACHER, ADMIN, OPERATOR, ACCOUNTANT, HEADMASTER, OTHER
    }
}
