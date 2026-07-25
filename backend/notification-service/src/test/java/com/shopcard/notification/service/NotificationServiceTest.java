package com.shopcard.notification.service;

import com.shopcard.notification.domain.model.Notification;
import com.shopcard.notification.domain.ports.out.EmailSenderPort;
import com.shopcard.notification.domain.ports.out.NotificationRepositoryPort;
import com.shopcard.notification.domain.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepositoryPort repositoryPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createNotification_shouldSaveAndReturnNotification() {
        UUID userId = UUID.randomUUID();
        String message = "Your order has been shipped!";

        when(repositoryPort.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Notification result = notificationService.createNotification(userId, message);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getMessage()).isEqualTo(message);
        assertThat(result.getIsRead()).isFalse();
        assertThat(result.getTimestamp()).isNotNull();

        verify(repositoryPort).save(any(Notification.class));
    }

    @Test
    void getNotifications_shouldReturnNotificationsInOrder() {
        UUID userId = UUID.randomUUID();
        List<Notification> mockList = List.of(
                Notification.builder().id(2L).userId(userId).message("Latest").timestamp(ZonedDateTime.now()).build(),
                Notification.builder().id(1L).userId(userId).message("Oldest").timestamp(ZonedDateTime.now().minusMinutes(5)).build()
        );

        when(repositoryPort.findByUserIdOrderByTimestampDesc(userId)).thenReturn(mockList);

        List<Notification> result = notificationService.getNotifications(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMessage()).isEqualTo("Latest");
        assertThat(result.get(1).getMessage()).isEqualTo("Oldest");

        verify(repositoryPort).findByUserIdOrderByTimestampDesc(userId);
    }

    @Test
    void markAsRead_shouldSetIsReadToTrue() {
        Long notificationId = 123L;
        Notification notification = Notification.builder()
                .id(notificationId)
                .userId(UUID.randomUUID())
                .message("Test message")
                .isRead(false)
                .build();

        when(repositoryPort.findById(notificationId)).thenReturn(Optional.of(notification));
        when(repositoryPort.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.markAsRead(notificationId);

        assertThat(notification.getIsRead()).isTrue();
        verify(repositoryPort).findById(notificationId);
        verify(repositoryPort).save(notification);
    }

    @Test
    void markAsRead_notFound_shouldThrowException() {
        Long notificationId = 123L;
        when(repositoryPort.findById(notificationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(notificationId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Notification not found");

        verify(repositoryPort, never()).save(any());
    }

    @Test
    void sendEmail_shouldDelegateToEmailSenderPort() {
        String to = "user@test.com";
        String subject = "Hello";
        String body = "Body content";

        notificationService.sendEmail(to, subject, body);

        verify(emailSenderPort).sendEmail(to, subject, body);
    }
}
