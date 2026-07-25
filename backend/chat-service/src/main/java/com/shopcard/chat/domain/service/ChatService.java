package com.shopcard.chat.domain.service;

import com.shopcard.chat.domain.model.ChatMessage;
import com.shopcard.chat.domain.ports.in.GetChatHistoryUseCase;
import com.shopcard.chat.domain.ports.in.SendMessageUseCase;
import com.shopcard.chat.domain.ports.out.ChatMessageRepositoryPort;
import com.shopcard.chat.domain.ports.out.NotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service("domainChatService")
@RequiredArgsConstructor
public class ChatService implements SendMessageUseCase, GetChatHistoryUseCase {

    private final ChatMessageRepositoryPort chatMessageRepositoryPort;
    private final NotificationPort notificationPort;

    @Override
    @Transactional
    public ChatMessage sendMessage(ChatMessage message) {
        if (message.getTimestamp() == null) {
            message.setTimestamp(ZonedDateTime.now());
        }
        ChatMessage saved = chatMessageRepositoryPort.save(message);
        notificationPort.notifyRecipient(saved);
        return saved;
    }

    @Override
    public List<ChatMessage> getChatHistory(UUID senderId, UUID recipientId) {
        return chatMessageRepositoryPort.findChatHistory(senderId, recipientId);
    }
}
