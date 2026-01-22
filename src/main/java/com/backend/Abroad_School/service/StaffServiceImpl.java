package com.backend.Abroad_School.service;

import com.backend.Abroad_School.dto.StaffDTO;
import com.backend.Abroad_School.exception.ResourceNotFoundException;
import com.backend.Abroad_School.model.SalaryStructure;
import com.backend.Abroad_School.model.Staff;
import com.backend.Abroad_School.repository.SalaryStructureRepository;
import com.backend.Abroad_School.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final SalaryStructureRepository salaryStructureRepository;

    @Override
    public StaffDTO createStaff(StaffDTO dto) {
        Staff s = Staff.builder()
                .fullName(dto.getFullName())
                .cnic(dto.getCnic())
                .dateOfBirth(dto.getDateOfBirth())
                .contactNumber(dto.getContactNumber())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .designation(dto.getDesignation())
                .active(true)
                .build();

        if (dto.getSalaryStructureId() != null) {
            SalaryStructure st = salaryStructureRepository.findById(dto.getSalaryStructureId())
                    .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure not found: " + dto.getSalaryStructureId()));
            s.setSalaryStructure(st);
        }

        Staff saved = staffRepository.save(s);
        dto.setId(saved.getId());
        return dto;
    }

    @Override
    public StaffDTO updateStaff(Long id, StaffDTO dto) {
        Staff s = staffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));
        s.setFullName(dto.getFullName());
        s.setCnic(dto.getCnic());
        s.setDateOfBirth(dto.getDateOfBirth());
        s.setContactNumber(dto.getContactNumber());
        s.setEmail(dto.getEmail());
        s.setAddress(dto.getAddress());
        s.setDesignation(dto.getDesignation());

        if (dto.getSalaryStructureId() != null) {
            SalaryStructure st = salaryStructureRepository.findById(dto.getSalaryStructureId())
                    .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure not found: " + dto.getSalaryStructureId()));
            s.setSalaryStructure(st);
        } else {
            s.setSalaryStructure(null);
        }

        staffRepository.save(s);
        dto.setId(s.getId());
        return dto;
    }

    @Override
    public Staff getStaff(Long id) {
        return staffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));
    }

    @Override
    public List<Staff> listAllActiveStaff() {
        return staffRepository.findByActiveTrue();
    }

    @Override
    public void deactivateStaff(Long id) {
        Staff s = staffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));
        s.setActive(false);
        staffRepository.save(s);
    }
}
