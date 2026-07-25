package com.shopcard.inventory.infrastructure.messaging.adapter;

import com.shopcard.inventory.domain.ports.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishInventoryResult(String topic, String orderId, UUID shopId, String status, String reason) {
        String safeReason = reason == null ? "" : reason.replace("\"", "'");
        String message = String.format("{\"orderId\":\"%s\", \"shopId\":\"%s\", \"status\":\"%s\", \"reason\":\"%s\"}", 
                orderId, shopId, status, safeReason);
        kafkaTemplate.send(topic, orderId, message);
    }
}
