package com.backend.Abroad_School.controller;

import com.backend.Abroad_School.model.FeePlan;
import com.backend.Abroad_School.service.FeePlanService;
import com.backend.Abroad_School.dto.FeePlanRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fee-plans")
public class FeePlanController {

    private final FeePlanService feePlanService;

    
    public FeePlanController(FeePlanService feePlanService) {
        this.feePlanService = feePlanService;
    }

    @PostMapping
    public ResponseEntity<FeePlan> createFeePlan(@RequestBody FeePlanRequest request) {

        FeePlan feePlan = feePlanService.createFeePlan(
                request.getName(),
                request.getFeeHeadIds(),
                request.isMonthly()
        );

        return ResponseEntity.ok(feePlan);
    }
}
