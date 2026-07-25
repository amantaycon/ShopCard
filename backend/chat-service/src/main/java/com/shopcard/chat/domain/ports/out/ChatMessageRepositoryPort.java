package com.shopcard.chat.domain.ports.out;

import com.shopcard.chat.domain.model.ChatMessage;
import java.util.List;
import java.util.UUID;

public interface ChatMessageRepositoryPort {
    ChatMessage save(ChatMessage message);
    List<ChatMessage> findChatHistory(UUID senderId, UUID recipientId);
}
