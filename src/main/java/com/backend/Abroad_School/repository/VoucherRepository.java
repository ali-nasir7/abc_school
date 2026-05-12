package com.backend.Abroad_School.repository;

import com.backend.Abroad_School.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    // Existing — unchanged
    @Query("SELECT v FROM Voucher v WHERE v.paid = false")
    List<Voucher> findAllUnpaidVouchers();

    @Query("SELECT v FROM Voucher v WHERE v.student.id = :studentId AND v.paid = false")
    List<Voucher> findUnpaidVouchersByStudent(@Param("studentId") Long studentId);

    // NEW — voucher send karna hai (unpaid, active student, abhi tak send nahi hua)
    @Query("SELECT v FROM Voucher v " +
           "WHERE v.paid = false " +
           "AND v.student.studentStatus = 'ACTIVE' " +
           "AND v.voucherSent = false " +
           "AND v.dueDate IS NOT NULL")
    List<Voucher> findUnsentVouchers();

    // NEW — reminder bhejni hai (unpaid, active, reminder abhi tak nahi gayi)
    @Query("SELECT v FROM Voucher v " +
           "WHERE v.paid = false " +
           "AND v.student.studentStatus = 'ACTIVE' " +
           "AND v.voucherSent = true " +
           "AND v.reminderSent = false " +
           "AND v.dueDate IS NOT NULL")
    List<Voucher> findVouchersNeedingReminder();

    // NEW — check karo ke is student ka is month/year mein voucher already hai
    @Query("SELECT COUNT(v) > 0 FROM Voucher v " +
           "WHERE v.student.id = :studentId " +
           "AND v.voucherMonth = :month " +
           "AND v.voucherYear = :year")
    boolean existsByStudentAndMonthAndYear(
        @Param("studentId") Long studentId,
        @Param("month") int month,
        @Param("year") int year
    );
}