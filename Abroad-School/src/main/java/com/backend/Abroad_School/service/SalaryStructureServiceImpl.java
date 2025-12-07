package com.backend.Abroad_School.service;

import com.backend.Abroad_School.dto.SalaryStructureDTO;
import com.backend.Abroad_School.exception.ResourceNotFoundException;
import com.backend.Abroad_School.model.SalaryStructure;
import com.backend.Abroad_School.repository.SalaryStructureRepository;
//import com.backend.Abroad_School.service.SalaryStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalaryStructureServiceImpl implements SalaryStructureService {

    private final SalaryStructureRepository repo;

    @Override
    public SalaryStructureDTO create(SalaryStructureDTO dto) {
        SalaryStructure s = SalaryStructure.builder()
                .name(dto.getName())
                .basicPay(dto.getBasicPay())
                .allowances(dto.getAllowances())
                .deductions(dto.getDeductions())
                .tax(dto.getTax())
                .active(true)
                .build();
        SalaryStructure saved = repo.save(s);
        dto.setId(saved.getId());
        return dto;
    }

    @Override
    public SalaryStructureDTO update(Long id, SalaryStructureDTO dto) {
        SalaryStructure existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure not found: " + id));
        existing.setName(dto.getName());
        existing.setBasicPay(dto.getBasicPay());
        existing.setAllowances(dto.getAllowances());
        existing.setDeductions(dto.getDeductions());
        existing.setTax(dto.getTax());
        repo.save(existing);
        dto.setId(existing.getId());
        return dto;
    }

    @Override
    public SalaryStructure getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("SalaryStructure not found: " + id));
    }

    @Override
    public List<SalaryStructure> listAll() {
        return repo.findAll();
    }
}
