package com.backend.Abroad_School.service;

import com.backend.Abroad_School.model.Voucher;

public interface NotificationService {
    void sendLateFeeNotificationToParent(Voucher voucher);
    void sendPaymentReceivedNotificationToParent(Voucher voucher);
}
