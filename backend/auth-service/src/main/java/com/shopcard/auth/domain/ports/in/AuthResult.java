package com.shopcard.auth.domain.ports.in;

import java.util.Set;
import java.util.UUID;

public record AuthResult(
    UUID userId,
    String email,
    String firstName,
    String lastName,
    Boolean emailVerified,
    Set<String> roles,
    String accessToken,
    String refreshToken
) {}
