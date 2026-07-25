package com.shopcard.chat.infrastructure.persistence.repository;

import com.shopcard.chat.infrastructure.persistence.entity.ChatMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageJpaEntity, Long> {

    @Query("SELECT m FROM ChatMessageJpaEntity m WHERE " +
           "(m.senderId = :user1 AND m.recipientId = :user2) OR " +
           "(m.senderId = :user2 AND m.recipientId = :user1) " +
           "ORDER BY m.timestamp ASC")
    List<ChatMessageJpaEntity> findChatHistory(@Param("user1") UUID user1, @Param("user2") UUID user2);
}
