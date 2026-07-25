package com.shopcard.order.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcard.order.domain.ports.in.HandleInventoryReservationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryReservationConsumer {

    private final HandleInventoryReservationUseCase handleInventoryReservationUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "inventory.reserved", groupId = "order-group")
    public void handleInventoryReserved(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String orderId = root.get("orderId").asText();
            handleInventoryReservationUseCase.handleInventoryReservation(UUID.fromString(orderId), "SUCCESS", "");
        } catch (Exception e) {
            System.err.println("Error processing inventory.reserved event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "inventory.failed", groupId = "order-group")
    public void handleInventoryFailed(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String orderId = root.get("orderId").asText();
            String reason = root.get("reason").asText();
            handleInventoryReservationUseCase.handleInventoryReservation(UUID.fromString(orderId), "FAILED", reason);
        } catch (Exception e) {
            System.err.println("Error processing inventory.failed event: " + e.getMessage());
        }
    }
}
