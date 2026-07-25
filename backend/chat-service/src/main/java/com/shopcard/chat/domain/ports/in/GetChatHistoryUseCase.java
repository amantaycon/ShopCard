package com.shopcard.chat.domain.ports.in;

import com.shopcard.chat.domain.model.ChatMessage;
import java.util.List;
import java.util.UUID;

public interface GetChatHistoryUseCase {
    List<ChatMessage> getChatHistory(UUID senderId, UUID recipientId);
}
