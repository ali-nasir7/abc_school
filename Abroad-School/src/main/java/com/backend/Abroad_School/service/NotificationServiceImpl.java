package com.backend.Abroad_School.service;

import com.backend.Abroad_School.model.Voucher;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendLateFeeNotificationToParent(Voucher voucher) {
        // // implement krna hai
        System.out.println("Notification (Late Fee) -> Student: " + voucher.getStudent().getFullName() +
                ", Amount: " + voucher.getLateFee());
    }

    @Override
    public void sendPaymentReceivedNotificationToParent(Voucher voucher) {
        // implement krna hai
        System.out.println("Notification (Payment Received) -> Student: " + voucher.getStudent().getFullName() +
                ", Amount Paid: " + voucher.getTotalAmount());
    }
}
