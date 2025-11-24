package com.backend.Abroad_School.service;

import com.backend.Abroad_School.model.Voucher;

import java.util.List;

public interface VoucherService {
   
    void applyLateFeesForAllPendingStudents();

  
    byte[] generateVoucherPDF(Long voucherId);

    Voucher createVoucher(Voucher voucher);

    Voucher markVoucherPaid(Long voucherId);
    
   
    List<Voucher> getAllUnpaidVouchers();
    void saveVoucher(Voucher voucher);
}
