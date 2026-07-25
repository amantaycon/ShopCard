package com.shopcard.catalog.infrastructure.messaging.adapter;

import com.shopcard.catalog.domain.model.Product;
import com.shopcard.catalog.domain.ports.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishProductEvent(String eventType, Product product) {
        kafkaTemplate.send("catalog.product.updated", product.getId().toString(), 
            String.format("{\"productId\":\"%s\", \"sku\":\"%s\", \"shopId\":\"%s\", \"action\":\"%s\", \"price\":%s}", 
                product.getId(), product.getSku(), product.getShopId(), eventType, product.getPrice()));
    }
}
