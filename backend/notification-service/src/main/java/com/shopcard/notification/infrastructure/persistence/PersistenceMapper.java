package com.shopcard.notification.infrastructure.persistence;

import com.shopcard.notification.domain.model.Notification;
import com.shopcard.notification.infrastructure.persistence.entity.NotificationJpaEntity;

public class PersistenceMapper {

    public static Notification toDomain(NotificationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Notification.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .message(entity.getMessage())
                .isRead(entity.getIsRead())
                .timestamp(entity.getTimestamp())
                .build();
    }

    public static NotificationJpaEntity toJpa(Notification domain) {
        if (domain == null) {
            return null;
        }
        return NotificationJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .message(domain.getMessage())
                .isRead(domain.getIsRead())
                .timestamp(domain.getTimestamp())
                .build();
    }
}
