package com.shopcard.gateway.domain.ports;

public interface TokenValidator {
    TokenClaims validateAndGetClaims(String token);
}
