package com.shopcard.chat.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    private Long id;
    private UUID senderId;
    private UUID recipientId;
    private String content;
    private ZonedDateTime timestamp;
}
