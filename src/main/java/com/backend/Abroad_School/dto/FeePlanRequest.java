package com.backend.Abroad_School.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeePlanRequest {
    private String name;
    private List<Long> feeHeadIds;
    private boolean monthly;
}
