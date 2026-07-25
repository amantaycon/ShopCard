package com.shopcard.order.infrastructure.messaging.adapter;

import com.shopcard.order.domain.model.Order;
import com.shopcard.order.domain.ports.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishOrderEvent(String topic, Order order) {
        try {
            String itemsJson = order.getItems().stream()
                    .map(item -> String.format("{\"productId\":\"%s\", \"quantity\":%d}", item.getProductId(), item.getQuantity()))
                    .collect(Collectors.joining(","));
            String message = String.format("{\"orderId\":\"%s\", \"shopId\":\"%s\", \"customerId\":\"%s\", \"items\":[%s]}", 
                    order.getId(), order.getShopId(), order.getCustomerId(), itemsJson);

            kafkaTemplate.send(topic, order.getId().toString(), message);
        } catch (Exception e) {
            System.err.println("Failed to publish Kafka event for topic " + topic + ": " + e.getMessage());
        }
    }
}
