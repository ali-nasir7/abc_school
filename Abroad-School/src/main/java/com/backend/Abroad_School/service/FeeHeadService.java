package com.backend.Abroad_School.service;
import com.backend.Abroad_School.model.FeeHead;
import java.util.List;

public interface FeeHeadService {
    FeeHead createFeeHead(FeeHead feeHead);
    List<FeeHead> getAllFeeHeads();
    FeeHead updateFeeHead(Long id, FeeHead feeHead);
    void deleteFeeHead(Long id);
}

