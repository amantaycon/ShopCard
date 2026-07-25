package com.shopcard.auth.domain.ports.in;

public record ResetPasswordCommand(
    String token,
    String newPassword
) {}
