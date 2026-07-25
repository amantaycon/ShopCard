package com.shopcard.auth.domain.ports.out;

import com.shopcard.auth.domain.model.User;

public interface EventPublisherPort {
    void publishUserRegisteredEvent(User user, String token);
    void publishPasswordResetEvent(User user, String token);
    void publishEmailVerificationEvent(String email, String code);
}
