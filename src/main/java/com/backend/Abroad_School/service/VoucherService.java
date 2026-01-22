package com.backend.Abroad_School.service;

import com.backend.Abroad_School.dto.VoucherDTO;
import com.backend.Abroad_School.dto.VoucherRequest;
import com.backend.Abroad_School.model.Voucher;

import java.util.List;

public interface VoucherService {

    void applyLateFeesForAllPendingStudents();

    byte[] generateVoucherPDF(Long voucherId);

    Voucher createVoucher(VoucherRequest request);

    Voucher markVoucherPaid(Long voucherId);

    List<Voucher> getAllUnpaidVouchers();

    List<Voucher> getUnpaidVouchersByStudent(Long studentId);

    VoucherDTO mapToDTO(Voucher voucher);

    List<VoucherDTO> mapListToDTO(List<Voucher> vouchers);

    void saveVoucher(Voucher voucher);
}
