package com.shopcard.auth.domain.ports.out;

import com.shopcard.auth.domain.model.VerificationToken;

import java.util.Optional;

public interface VerificationTokenRepositoryPort {
    Optional<VerificationToken> findByToken(String token);
    VerificationToken save(VerificationToken token);
    void delete(VerificationToken token);
}
