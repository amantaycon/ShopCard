package com.shopcard.notification.domain.ports.in;

public interface SendEmailUseCase {
    void sendEmail(String toEmail, String subject, String textContent);
}
