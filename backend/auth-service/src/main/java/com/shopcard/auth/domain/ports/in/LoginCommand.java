package com.shopcard.auth.domain.ports.in;

public record LoginCommand(
    String email,
    String password
) {}
