package com.shopcard.auth.domain.ports.in;

public interface VerifyCodeUseCase {
    void verifyCode(String email, String code);
}
