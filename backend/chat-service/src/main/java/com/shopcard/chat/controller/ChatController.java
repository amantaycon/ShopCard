package com.shopcard.chat.controller;

import com.shopcard.chat.domain.model.ChatMessage;
import com.shopcard.chat.domain.ports.in.GetChatHistoryUseCase;
import com.shopcard.chat.domain.ports.in.SendMessageUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SendMessageUseCase sendMessageUseCase;
    private final GetChatHistoryUseCase getChatHistoryUseCase;

    // WebSocket Message receiver mapping: `/app/chat.send`
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message) {
        sendMessageUseCase.sendMessage(message);
    }

    // REST HTTP endpoint for history loading
    @GetMapping("/api/v1/chats/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam UUID withUserId
    ) {
        List<ChatMessage> history = getChatHistoryUseCase.getChatHistory(
                UUID.fromString(userId),
                withUserId
        );
        return ResponseEntity.ok(history);
    }
}
