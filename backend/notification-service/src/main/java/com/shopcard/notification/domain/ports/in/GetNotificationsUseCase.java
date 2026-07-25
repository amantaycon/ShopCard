package com.shopcard.notification.domain.ports.in;

import com.shopcard.notification.domain.model.Notification;
import java.util.List;
import java.util.UUID;

public interface GetNotificationsUseCase {
    List<Notification> getNotifications(UUID userId);
}
