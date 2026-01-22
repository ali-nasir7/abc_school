package com.backend.Abroad_School.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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
    @JsonManagedReference
    @JsonIgnore
   private List<Student> students;

}   

