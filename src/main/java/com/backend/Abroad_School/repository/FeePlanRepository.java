package com.backend.Abroad_School.repository;



import com.backend.Abroad_School.model.FeePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeePlanRepository extends JpaRepository<FeePlan, Long> { }
