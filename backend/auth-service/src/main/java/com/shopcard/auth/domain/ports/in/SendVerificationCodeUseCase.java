package com.shopcard.auth.domain.ports.in;

public interface SendVerificationCodeUseCase {
    void sendVerificationCode(String email);
}
