package com.backend.Abroad_School.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_grnumber", columnList = "grNumber"),
        @Index(name = "idx_parent_contact1", columnList = "parentContact1")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( unique = true)
    private String grNumber; // e.g., GR-2025-0001

    @Column(nullable = false)
    private String fullName;

    private String fatherName;
    private String motherName;
    private String fatherCnic;
    private String motherCnic;

    private LocalDate dateOfBirth;
    private String className; 
    private String section; 
    private String groupName;
    private Integer rollNumber;
    private LocalDate admissionDate;
    private String previousSchool;

    private String parentContact1;
    private String parentContact2;
    private String address;
    private String bFormNumber;

    @Enumerated(EnumType.STRING)
    private StudentStatus studentStatus;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents;

 @ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "fee_plan_id")
private FeePlan feePlan;
private int lateFee ;
public void addLateFee(double fee) {
        this.lateFee += fee;
    }

    public int getLateFee() {
        return lateFee;
    }

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<Voucher> vouchers;


}

