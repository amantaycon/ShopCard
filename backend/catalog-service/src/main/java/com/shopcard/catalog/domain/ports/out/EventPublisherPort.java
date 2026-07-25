package com.shopcard.catalog.domain.ports.out;

import com.shopcard.catalog.domain.model.Product;

public interface EventPublisherPort {
    void publishProductEvent(String eventType, Product product);
}
