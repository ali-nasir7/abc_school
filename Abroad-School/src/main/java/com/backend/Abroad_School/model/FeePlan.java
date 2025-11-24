package com.backend.Abroad_School.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; 

    @ManyToMany
    @JoinTable(
        name = "feeplan_feehead",
        joinColumns = @JoinColumn(name = "feeplan_id"),
        inverseJoinColumns = @JoinColumn(name = "feehead_id")
    )
    private List<FeeHead> feeHeads;

    private boolean monthly; // true = monthly, false = one-time
    @OneToMany(mappedBy = "feePlan")
private List<Student> students;

}   

