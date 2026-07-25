package com.shopcard.notification.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcard.notification.domain.ports.in.CreateNotificationUseCase;
import com.shopcard.notification.domain.ports.in.SendEmailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthEventConsumer {

    private final CreateNotificationUseCase createNotificationUseCase;
    private final SendEmailUseCase sendEmailUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "auth.user.registered", groupId = "notification-group")
    public void handleUserRegistered(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String userId = root.get("userId").asText();
            String email = root.get("email").asText();
            String firstName = root.get("firstName").asText();
            String token = root.get("verificationToken").asText();

            String verificationLink = "http://localhost:5173/verify-email?token=" + token;
            String emailBody = String.format("Hi %s,\n\nWelcome to ShopCard! Please verify your email by clicking the link below:\n%s\n\nBest regards,\nShopCard Team",
                    firstName, verificationLink);

            // Record live notification
            createNotificationUseCase.createNotification(UUID.fromString(userId), "Please verify your email address to complete registration.");

            // Dispatch SMTP email
            sendEmailUseCase.sendEmail(email, "ShopCard - Verify Your Email", emailBody);

        } catch (Exception e) {
            System.err.println("Error processing auth.user.registered notification event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "auth.password.reset", groupId = "notification-group")
    public void handlePasswordReset(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String email = root.get("email").asText();
            String firstName = root.get("firstName").asText();
            String token = root.get("resetToken").asText();

            String resetLink = "http://localhost:5173/reset-password?token=" + token;
            String emailBody = String.format("Hi %s,\n\nWe received a request to reset your password. Click the link below to enter a new password:\n%s\n\nThis link is valid for 15 minutes.\n\nBest regards,\nShopCard Team",
                    firstName, resetLink);

            // Dispatch reset email
            sendEmailUseCase.sendEmail(email, "ShopCard - Password Reset Request", emailBody);

        } catch (Exception e) {
            System.err.println("Error processing auth.password.reset notification event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "auth.email.verification", groupId = "notification-group")
    public void handleEmailVerification(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String email = root.get("email").asText();
            String code = root.get("code").asText();

            String emailBody = String.format("Welcome to ShopCard!\n\nYour 6-digit verification code is: %s\n\nThis code will expire in 15 minutes.\n\nBest regards,\nShopCard Team",
                    code);

            // Dispatch verification email
            sendEmailUseCase.sendEmail(email, "ShopCard - Email Verification Code", emailBody);

        } catch (Exception e) {
            System.err.println("Error processing auth.email.verification notification event: " + e.getMessage());
        }
    }
}
