package com.backend.Abroad_School.repository;

import com.backend.Abroad_School.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    @Query("SELECT v FROM Voucher v WHERE v.paid = false")
    List<Voucher> findAllUnpaidVouchers();
    @Query("SELECT v FROM Voucher v WHERE v.student.id = :studentId AND v.paid = false")
    List<Voucher> findUnpaidVouchersByStudent(@Param("studentId") Long studentId);
}
