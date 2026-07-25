package com.shopcard.auth.domain.ports.in;

public record GoogleLoginCommand(
    String idToken
) {}
