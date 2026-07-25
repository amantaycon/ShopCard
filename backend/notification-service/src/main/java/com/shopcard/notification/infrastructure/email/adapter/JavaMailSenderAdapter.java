package com.shopcard.notification.infrastructure.email.adapter;

import com.shopcard.notification.domain.ports.out.EmailSenderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JavaMailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String toEmail, String subject, String textContent) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(toEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(textContent);
            mailMessage.setFrom("no-reply@shopcard.com");
            
            mailSender.send(mailMessage);
            System.out.println("Email notification dispatched successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to dispatch email to " + toEmail + ": " + e.getMessage());
        }
    }
}
