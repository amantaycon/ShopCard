package com.shopcard.notification.domain.ports.out;

public interface EmailSenderPort {
    void sendEmail(String toEmail, String subject, String textContent);
}
