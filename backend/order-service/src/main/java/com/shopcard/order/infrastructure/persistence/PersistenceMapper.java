package com.shopcard.order.infrastructure.persistence;

import com.shopcard.order.domain.model.Order;
import com.shopcard.order.domain.model.OrderItem;
import com.shopcard.order.domain.model.OrderStatusLog;
import com.shopcard.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.shopcard.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.shopcard.order.infrastructure.persistence.entity.OrderStatusLogJpaEntity;

import java.util.stream.Collectors;

public class PersistenceMapper {

    public static Order toDomain(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Order domain = Order.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .shopId(entity.getShopId())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .pickupCodeHash(entity.getPickupCodeHash())
                .pickupCodeExpiry(entity.getPickupCodeExpiry())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();

        if (entity.getItems() != null) {
            domain.setItems(entity.getItems().stream()
                    .map(PersistenceMapper::toDomain)
                    .collect(Collectors.toList()));
        }
        return domain;
    }

    public static OrderJpaEntity toJpa(Order domain) {
        if (domain == null) {
            return null;
        }
        OrderJpaEntity entity = OrderJpaEntity.builder()
                .id(domain.getId())
                .customerId(domain.getCustomerId())
                .shopId(domain.getShopId())
                .totalAmount(domain.getTotalAmount())
                .status(domain.getStatus())
                .pickupCodeHash(domain.getPickupCodeHash())
                .pickupCodeExpiry(domain.getPickupCodeExpiry())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();

        if (domain.getItems() != null) {
            entity.setItems(domain.getItems().stream()
                    .map(item -> toJpa(item, entity))
                    .collect(Collectors.toList()));
        }
        return entity;
    }

    public static OrderItem toDomain(OrderItemJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return OrderItem.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .name(entity.getName())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
                .build();
    }

    public static OrderItemJpaEntity toJpa(OrderItem domain, OrderJpaEntity orderEntity) {
        if (domain == null) {
            return null;
        }
        return OrderItemJpaEntity.builder()
                .id(domain.getId())
                .order(orderEntity)
                .productId(domain.getProductId())
                .name(domain.getName())
                .price(domain.getPrice())
                .quantity(domain.getQuantity())
                .build();
    }

    public static OrderStatusLog toDomain(OrderStatusLogJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return OrderStatusLog.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .fromStatus(entity.getFromStatus())
                .toStatus(entity.getToStatus())
                .remarks(entity.getRemarks())
                .changedBy(entity.getChangedBy())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static OrderStatusLogJpaEntity toJpa(OrderStatusLog domain) {
        if (domain == null) {
            return null;
        }
        return OrderStatusLogJpaEntity.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .fromStatus(domain.getFromStatus())
                .toStatus(domain.getToStatus())
                .remarks(domain.getRemarks())
                .changedBy(domain.getChangedBy())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
