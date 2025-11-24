package com.backend.Abroad_School.service;

import com.backend.Abroad_School.dto.StudentDTO;
import com.backend.Abroad_School.model.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    //  Entity → DTO
    public StudentDTO toDto(Student student) {
        if (student == null) return null;

        return StudentDTO.builder()
                .id(student.getId())
                .fullName(student.getFullName())
                .fatherName(student.getFatherName())
                .motherName(student.getMotherName())
                .fatherCnic(student.getFatherCnic())
                .motherCnic(student.getMotherCnic())
                .bFormNumber(student.getBFormNumber())
                .address(student.getAddress())
                .dateOfBirth(student.getDateOfBirth())
                .admissionDate(student.getAdmissionDate())
                .parentContact1(student.getParentContact1())
                .parentContact2(student.getParentContact2())
                .previousSchool(student.getPreviousSchool())
                .className(student.getClassName())
                .groupName(student.getGroupName())
                .section(student.getSection())
                .grNumber(student.getGrNumber())
                .rollNumber(student.getRollNumber())
                .studentStatus(student.getStudentStatus())
                .build();
    }

    //  DTO → Entity
    public Student toEntity(StudentDTO dto) {
        if (dto == null) return null;

        Student student = new Student();
        student.setId(dto.getId());
        student.setFullName(dto.getFullName());
        student.setFatherName(dto.getFatherName());
        student.setMotherName(dto.getMotherName());
        student.setFatherCnic(dto.getFatherCnic());
        student.setMotherCnic(dto.getMotherCnic());
        student.setBFormNumber(dto.getBFormNumber());
        student.setAddress(dto.getAddress());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setAdmissionDate(dto.getAdmissionDate());
        student.setParentContact1(dto.getParentContact1());
        student.setParentContact2(dto.getParentContact2());
        student.setPreviousSchool(dto.getPreviousSchool());
        student.setClassName(dto.getClassName());
        student.setGroupName(dto.getGroupName());
        student.setSection(dto.getSection());
        student.setGrNumber(dto.getGrNumber());
        student.setRollNumber(dto.getRollNumber());
        student.setStudentStatus(dto.getStudentStatus());
        dto.setFeePlanId(student.getFeePlan() != null ? student.getFeePlan().getId() : null);
        return student;
    }
}
