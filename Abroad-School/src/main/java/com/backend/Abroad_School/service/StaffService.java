package com.backend.Abroad_School.service;

import com.backend.Abroad_School.dto.StaffDTO;
import com.backend.Abroad_School.model.Staff;

import java.util.List;

public interface StaffService {
    StaffDTO createStaff(StaffDTO dto);
    StaffDTO updateStaff(Long id, StaffDTO dto);
    Staff getStaff(Long id);
    List<Staff> listAllActiveStaff();
    void deactivateStaff(Long id);
}
