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
public class OrderEventConsumer {

    private final CreateNotificationUseCase createNotificationUseCase;
    private final SendEmailUseCase sendEmailUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.preparing", groupId = "notification-group")
    public void handleOrderPreparing(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String orderId = root.get("orderId").asText();
            String customerId = root.get("customerId").asText();

            String notifMsg = "Your order #" + orderId + " has been accepted and is being prepared!";
            createNotificationUseCase.createNotification(UUID.fromString(customerId), notifMsg);
            
            // Send email
            sendEmailUseCase.sendEmail(
                    customerId + "@shopcard-user.com",
                    "ShopCard - Order Accepted",
                    notifMsg
            );
        } catch (Exception e) {
            System.err.println("Error processing order.preparing notification: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "order.ready-for-pickup", groupId = "notification-group")
    public void handleOrderReady(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String orderId = root.get("orderId").asText();
            String customerId = root.get("customerId").asText();

            String notifMsg = "Your order #" + orderId + " is ready for pickup! Show the OTP code from your dashboard to the shop owner.";
            createNotificationUseCase.createNotification(UUID.fromString(customerId), notifMsg);

            sendEmailUseCase.sendEmail(
                    customerId + "@shopcard-user.com",
                    "ShopCard - Order Ready for Pickup!",
                    notifMsg + "\n\nVerify this order at the counter to collect your items."
            );
        } catch (Exception e) {
            System.err.println("Error processing order.ready-for-pickup notification: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "order.completed", groupId = "notification-group")
    public void handleOrderCompleted(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String orderId = root.get("orderId").asText();
            String customerId = root.get("customerId").asText();

            String notifMsg = "Thank you! Your order #" + orderId + " has been picked up successfully!";
            createNotificationUseCase.createNotification(UUID.fromString(customerId), notifMsg);

            sendEmailUseCase.sendEmail(
                    customerId + "@shopcard-user.com",
                    "ShopCard - Order Completed",
                    notifMsg + "\n\nWe hope you enjoy your purchase!"
            );
        } catch (Exception e) {
            System.err.println("Error processing order.completed notification: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "order.cancelled", groupId = "notification-group")
    public void handleOrderCancelled(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String orderId = root.get("orderId").asText();
            String customerId = root.get("customerId").asText();

            String notifMsg = "Alert: Your order #" + orderId + " has been cancelled.";
            createNotificationUseCase.createNotification(UUID.fromString(customerId), notifMsg);

            sendEmailUseCase.sendEmail(
                    customerId + "@shopcard-user.com",
                    "ShopCard - Order Cancelled",
                    notifMsg
            );
        } catch (Exception e) {
            System.err.println("Error processing order.cancelled notification: " + e.getMessage());
        }
    }
}
