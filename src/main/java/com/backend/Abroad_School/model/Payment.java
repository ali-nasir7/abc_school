package com.backend.Abroad_School.model;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Student student;

    @ManyToOne
    private FeePlan feePlan;

    private double amountPaid;
    private double discount ;
    private double lateFee ;

    private LocalDate paymentDate;
}
