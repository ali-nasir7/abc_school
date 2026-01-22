package com.backend.Abroad_School.service;

import com.backend.Abroad_School.dto.SalaryStructureDTO;
import com.backend.Abroad_School.model.SalaryStructure;

import java.util.List;

public interface SalaryStructureService {
    SalaryStructureDTO create(SalaryStructureDTO dto);
    SalaryStructureDTO update(Long id, SalaryStructureDTO dto);
    SalaryStructure getById(Long id);
    List<SalaryStructure> listAll();
}
