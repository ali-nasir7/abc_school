package com.backend.Abroad_School.service;

import com.backend.Abroad_School.model.FeeHead;
import com.backend.Abroad_School.model.FeePlan;
import com.backend.Abroad_School.repository.FeeHeadRepository;
import com.backend.Abroad_School.repository.FeePlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeePlanService {

    private final FeePlanRepository feePlanRepository;
    private final FeeHeadRepository feeHeadRepository;

    public FeePlanService(FeePlanRepository feePlanRepository,
                          FeeHeadRepository feeHeadRepository) {
        this.feePlanRepository = feePlanRepository;
        this.feeHeadRepository = feeHeadRepository;
    }

    
    public FeePlan createFeePlan(String name, List<Long> feeHeadIds, boolean monthly) {

        List<FeeHead> feeHeads = feeHeadRepository.findAllById(feeHeadIds);

        if (feeHeads.isEmpty()) {
            throw new RuntimeException("No FeeHeads found for given IDs");
        }

        FeePlan feePlan = FeePlan.builder()
                .name(name)
                .monthly(monthly)
                .feeHeads(feeHeads)
                .build();

        return feePlanRepository.save(feePlan);
    }

    public FeePlan getFeePlanById(Long id) {
        return feePlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FeePlan not found: " + id));
    }
}
