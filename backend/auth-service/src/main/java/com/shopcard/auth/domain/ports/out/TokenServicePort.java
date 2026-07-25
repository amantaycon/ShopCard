package com.shopcard.auth.domain.ports.out;

import com.shopcard.auth.domain.model.User;

import java.util.Date;

public interface TokenServicePort {
    String generateToken(User user);
    String generateRefreshToken(User user);
    boolean isTokenValid(String token);
    String extractUserId(String token);
    Date extractExpiration(String token);
}
