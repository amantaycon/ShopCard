package com.shopcard.notification.domain.service;

import com.shopcard.notification.domain.model.Notification;
import com.shopcard.notification.domain.ports.in.CreateNotificationUseCase;
import com.shopcard.notification.domain.ports.in.GetNotificationsUseCase;
import com.shopcard.notification.domain.ports.in.MarkNotificationReadUseCase;
import com.shopcard.notification.domain.ports.in.SendEmailUseCase;
import com.shopcard.notification.domain.ports.out.EmailSenderPort;
import com.shopcard.notification.domain.ports.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service("domainNotificationService")
@RequiredArgsConstructor
public class NotificationService implements 
        CreateNotificationUseCase, 
        GetNotificationsUseCase, 
        MarkNotificationReadUseCase, 
        SendEmailUseCase {

    private final NotificationRepositoryPort notificationRepositoryPort;
    private final EmailSenderPort emailSenderPort;

    @Override
    @Transactional
    public Notification createNotification(UUID userId, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .isRead(false)
                .timestamp(ZonedDateTime.now())
                .build();
        return notificationRepositoryPort.save(notification);
    }

    @Override
    public List<Notification> getNotifications(UUID userId) {
        return notificationRepositoryPort.findByUserIdOrderByTimestampDesc(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepositoryPort.save(notification);
    }

    @Override
    public void sendEmail(String toEmail, String subject, String textContent) {
        emailSenderPort.sendEmail(toEmail, subject, textContent);
    }
}
