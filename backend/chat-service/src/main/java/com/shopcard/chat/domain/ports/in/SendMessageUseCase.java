package com.shopcard.chat.domain.ports.in;

import com.shopcard.chat.domain.model.ChatMessage;

public interface SendMessageUseCase {
    ChatMessage sendMessage(ChatMessage message);
}
