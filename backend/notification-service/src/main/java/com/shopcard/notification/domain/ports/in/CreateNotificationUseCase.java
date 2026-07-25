package com.shopcard.notification.domain.ports.in;

import com.shopcard.notification.domain.model.Notification;
import java.util.UUID;

public interface CreateNotificationUseCase {
    Notification createNotification(UUID userId, String message);
}
