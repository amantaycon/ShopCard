package com.shopcard.auth.domain.ports.out;

import com.shopcard.auth.domain.model.PasswordResetToken;
import com.shopcard.auth.domain.model.User;

import java.util.Optional;

public interface PasswordResetTokenRepositoryPort {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user);
    PasswordResetToken save(PasswordResetToken token);
    void delete(PasswordResetToken token);
}
