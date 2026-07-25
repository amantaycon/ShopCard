package com.shopcard.auth.infrastructure.messaging.adapter;

import com.shopcard.auth.domain.model.User;
import com.shopcard.auth.domain.ports.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishUserRegisteredEvent(User user, String token) {
        try {
            String message = String.format("{\"userId\":\"%s\", \"email\":\"%s\", \"firstName\":\"%s\", \"verificationToken\":\"%s\"}",
                    user.getId(), user.getEmail(), user.getFirstName(), token);
            kafkaTemplate.send("auth.user.registered", user.getEmail(), message);
        } catch (Exception e) {
            System.err.println("Failed to emit user.registered event: " + e.getMessage());
        }
    }

    @Override
    public void publishPasswordResetEvent(User user, String token) {
        try {
            String message = String.format("{\"email\":\"%s\", \"firstName\":\"%s\", \"resetToken\":\"%s\"}",
                    user.getEmail(), user.getFirstName(), token);
            kafkaTemplate.send("auth.password.reset", user.getEmail(), message);
        } catch (Exception e) {
            System.err.println("Failed to emit password.reset event: " + e.getMessage());
        }
    }

    @Override
    public void publishEmailVerificationEvent(String email, String code) {
        try {
            String message = String.format("{\"email\":\"%s\", \"code\":\"%s\"}", email, code);
            kafkaTemplate.send("auth.email.verification", email, message);
        } catch (Exception e) {
            System.err.println("Failed to emit email.verification event: " + e.getMessage());
        }
    }
}
