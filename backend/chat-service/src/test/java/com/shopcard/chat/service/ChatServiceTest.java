package com.shopcard.chat.service;

import com.shopcard.chat.domain.model.ChatMessage;
import com.shopcard.chat.domain.ports.out.ChatMessageRepositoryPort;
import com.shopcard.chat.domain.ports.out.NotificationPort;
import com.shopcard.chat.domain.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepositoryPort repositoryPort;

    @Mock
    private NotificationPort notificationPort;

    @InjectMocks
    private ChatService chatService;

    @Test
    void sendMessage_shouldSetTimestampSaveAndNotify() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        ChatMessage msg = ChatMessage.builder()
                .senderId(senderId)
                .recipientId(recipientId)
                .content("Hello World")
                .build();

        when(repositoryPort.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ChatMessage result = chatService.sendMessage(msg);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.getContent()).isEqualTo("Hello World");
        assertThat(result.getSenderId()).isEqualTo(senderId);
        assertThat(result.getRecipientId()).isEqualTo(recipientId);

        verify(repositoryPort).save(any(ChatMessage.class));
        verify(notificationPort).notifyRecipient(result);
    }

    @Test
    void getChatHistory_shouldRetrieveHistory() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        List<ChatMessage> mockHistory = List.of(
                ChatMessage.builder().senderId(user1).recipientId(user2).content("Hi").timestamp(ZonedDateTime.now()).build(),
                ChatMessage.builder().senderId(user2).recipientId(user1).content("Hello").timestamp(ZonedDateTime.now()).build()
        );

        when(repositoryPort.findChatHistory(user1, user2)).thenReturn(mockHistory);

        List<ChatMessage> result = chatService.getChatHistory(user1, user2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("Hi");
        assertThat(result.get(1).getContent()).isEqualTo("Hello");

        verify(repositoryPort).findChatHistory(user1, user2);
    }
}
