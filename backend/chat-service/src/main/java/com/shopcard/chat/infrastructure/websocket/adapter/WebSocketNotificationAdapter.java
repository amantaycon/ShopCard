package com.shopcard.chat.infrastructure.websocket.adapter;

import com.shopcard.chat.domain.model.ChatMessage;
import com.shopcard.chat.domain.ports.out.NotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketNotificationAdapter implements NotificationPort {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyRecipient(ChatMessage message) {
        // Send to recipient via STOMP user queue: `/user/{recipientId}/queue/messages`
        messagingTemplate.convertAndSendToUser(
                message.getRecipientId().toString(),
                "/queue/messages",
                message
        );
    }
}
