package com.shopcard.chat.infrastructure.persistence;

import com.shopcard.chat.domain.model.ChatMessage;
import com.shopcard.chat.infrastructure.persistence.entity.ChatMessageJpaEntity;

public class PersistenceMapper {

    public static ChatMessage toDomain(ChatMessageJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return ChatMessage.builder()
                .id(entity.getId())
                .senderId(entity.getSenderId())
                .recipientId(entity.getRecipientId())
                .content(entity.getContent())
                .timestamp(entity.getTimestamp())
                .build();
    }

    public static ChatMessageJpaEntity toJpa(ChatMessage domain) {
        if (domain == null) {
            return null;
        }
        return ChatMessageJpaEntity.builder()
                .id(domain.getId())
                .senderId(domain.getSenderId())
                .recipientId(domain.getRecipientId())
                .content(domain.getContent())
                .timestamp(domain.getTimestamp())
                .build();
    }
}
