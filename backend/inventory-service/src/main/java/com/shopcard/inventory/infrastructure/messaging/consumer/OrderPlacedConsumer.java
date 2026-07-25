package com.shopcard.inventory.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcard.inventory.domain.model.OrderItem;
import com.shopcard.inventory.domain.ports.in.ReleaseStockUseCase;
import com.shopcard.inventory.domain.ports.in.ReserveStockUseCase;
import com.shopcard.inventory.dto.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderPlacedConsumer {

    private final ReserveStockUseCase reserveStockUseCase;
    private final ReleaseStockUseCase releaseStockUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.placed", groupId = "inventory-group")
    public void handleOrderPlaced(String message) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);
            List<OrderItem> items = event.getItems().stream()
                    .map(item -> new OrderItem(item.getProductId(), item.getQuantity()))
                    .collect(Collectors.toList());
            reserveStockUseCase.reserveStock(event.getOrderId(), event.getShopId(), items);
        } catch (Exception e) {
            System.err.println("Error consuming order.placed event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "order.cancelled", groupId = "inventory-group")
    public void handleOrderCancelled(String message) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);
            List<OrderItem> items = event.getItems().stream()
                    .map(item -> new OrderItem(item.getProductId(), item.getQuantity()))
                    .collect(Collectors.toList());
            releaseStockUseCase.releaseStock(event.getOrderId(), event.getShopId(), items, false);
        } catch (Exception e) {
            System.err.println("Error consuming order.cancelled event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "order.completed", groupId = "inventory-group")
    public void handleOrderCompleted(String message) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);
            List<OrderItem> items = event.getItems().stream()
                    .map(item -> new OrderItem(item.getProductId(), item.getQuantity()))
                    .collect(Collectors.toList());
            releaseStockUseCase.releaseStock(event.getOrderId(), event.getShopId(), items, true);
        } catch (Exception e) {
            System.err.println("Error consuming order.completed event: " + e.getMessage());
        }
    }
}
