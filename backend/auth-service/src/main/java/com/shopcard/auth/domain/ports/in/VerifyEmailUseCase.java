package com.shopcard.auth.domain.ports.in;

public interface VerifyEmailUseCase {
    void verifyEmail(String token);
}
