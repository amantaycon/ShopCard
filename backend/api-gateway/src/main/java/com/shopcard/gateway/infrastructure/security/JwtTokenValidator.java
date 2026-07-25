package com.shopcard.gateway.infrastructure.security;

import com.shopcard.gateway.domain.ports.TokenClaims;
import com.shopcard.gateway.domain.ports.TokenValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenValidator implements TokenValidator {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public TokenClaims validateAndGetClaims(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String userId = claims.getSubject();
        String roles = claims.get("roles", String.class);
        if (roles == null) {
            roles = "";
        }

        return new TokenClaims(userId, roles);
    }
}
