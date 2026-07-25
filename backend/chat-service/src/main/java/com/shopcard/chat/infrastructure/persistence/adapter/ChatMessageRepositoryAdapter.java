package com.shopcard.chat.infrastructure.persistence.adapter;

import com.shopcard.chat.domain.model.ChatMessage;
import com.shopcard.chat.domain.ports.out.ChatMessageRepositoryPort;
import com.shopcard.chat.infrastructure.persistence.PersistenceMapper;
import com.shopcard.chat.infrastructure.persistence.entity.ChatMessageJpaEntity;
import com.shopcard.chat.infrastructure.persistence.repository.ChatMessageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatMessageRepositoryAdapter implements ChatMessageRepositoryPort {

    private final ChatMessageJpaRepository jpaRepository;

    @Override
    public ChatMessage save(ChatMessage message) {
        ChatMessageJpaEntity entity = PersistenceMapper.toJpa(message);
        ChatMessageJpaEntity saved = jpaRepository.save(entity);
        return PersistenceMapper.toDomain(saved);
    }

    @Override
    public List<ChatMessage> findChatHistory(UUID senderId, UUID recipientId) {
        return jpaRepository.findChatHistory(senderId, recipientId).stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
