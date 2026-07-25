package com.shopcard.auth.domain.ports.in;

public interface ResetPasswordUseCase {
    void resetPassword(ResetPasswordCommand command);
}
