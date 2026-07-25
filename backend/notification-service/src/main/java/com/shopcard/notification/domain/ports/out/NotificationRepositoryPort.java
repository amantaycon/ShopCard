package com.shopcard.notification.domain.ports.out;

import com.shopcard.notification.domain.model.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {
    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    List<Notification> findByUserIdOrderByTimestampDesc(UUID userId);
}
