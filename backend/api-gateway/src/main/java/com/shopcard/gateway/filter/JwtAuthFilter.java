package com.shopcard.gateway.filter;

import com.shopcard.gateway.domain.ports.TokenBlocklistChecker;
import com.shopcard.gateway.domain.ports.TokenClaims;
import com.shopcard.gateway.domain.ports.TokenValidator;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final TokenBlocklistChecker tokenBlocklistChecker;
    private final TokenValidator tokenValidator;

    public JwtAuthFilter(TokenBlocklistChecker tokenBlocklistChecker, TokenValidator tokenValidator) {
        super(Config.class);
        this.tokenBlocklistChecker = tokenBlocklistChecker;
        this.tokenValidator = tokenValidator;
    }

    public static class Config {
        // Configuration fields if needed
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization Header Format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            return tokenBlocklistChecker.isBlocklisted(token)
                    .flatMap(isBlocklisted -> {
                        if (Boolean.TRUE.equals(isBlocklisted)) {
                            return onError(exchange, "Token is blocklisted (Logged Out)", HttpStatus.UNAUTHORIZED);
                        }

                        try {
                            TokenClaims claims = tokenValidator.validateAndGetClaims(token);

                            ServerHttpRequest mutatedRequest = request.mutate()
                                    .header("X-User-Id", claims.userId())
                                    .header("X-User-Roles", claims.roles())
                                    .build();

                            return chain.filter(exchange.mutate().request(mutatedRequest).build());

                        } catch (Exception e) {
                            return onError(exchange, "Invalid JWT Token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                        }
                    });
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
}
