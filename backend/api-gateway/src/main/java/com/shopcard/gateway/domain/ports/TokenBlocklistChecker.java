package com.shopcard.gateway.domain.ports;

import reactor.core.publisher.Mono;

public interface TokenBlocklistChecker {
    Mono<Boolean> isBlocklisted(String token);
}
