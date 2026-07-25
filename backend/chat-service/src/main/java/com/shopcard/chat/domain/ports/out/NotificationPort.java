package com.shopcard.chat.domain.ports.out;

import com.shopcard.chat.domain.model.ChatMessage;

public interface NotificationPort {
    void notifyRecipient(ChatMessage message);
}
