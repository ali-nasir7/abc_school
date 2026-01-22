package com.backend.Abroad_School.service;
import com.backend.Abroad_School.model.FeeHead;
import com.backend.Abroad_School.repository.FeeHeadRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeeHeadServiceImpl implements FeeHeadService {

    private final FeeHeadRepository feeHeadRepository;

    public FeeHeadServiceImpl(FeeHeadRepository feeHeadRepository) {
        this.feeHeadRepository = feeHeadRepository;
    }

    @Override
    public FeeHead createFeeHead(FeeHead feeHead) {
        return feeHeadRepository.save(feeHead);
    }

    @Override
    public List<FeeHead> getAllFeeHeads() {
        return feeHeadRepository.findAll();
    }

    @Override
    public FeeHead updateFeeHead(Long id, FeeHead feeHead) {
        FeeHead existing = feeHeadRepository.findById(id).orElseThrow(() -> new RuntimeException("FeeHead not found"));
        existing.setName(feeHead.getName());
        existing.setAmount(feeHead.getAmount());
        existing.setActive(feeHead.isActive());
        return feeHeadRepository.save(existing);
    }

    @Override
    public void deleteFeeHead(Long id) {
        feeHeadRepository.deleteById(id);
    }
}

