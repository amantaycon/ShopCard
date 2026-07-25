package com.shopcard.auth.domain.ports.in;

public interface ForgotPasswordUseCase {
    void forgotPassword(String email);
}
