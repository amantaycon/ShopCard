package com.shopcard.gateway.infrastructure.cache;

import com.shopcard.gateway.domain.ports.TokenBlocklistChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RedisTokenBlocklistChecker implements TokenBlocklistChecker {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Boolean> isBlocklisted(String token) {
        return redisTemplate.hasKey("blocklist:" + token);
    }
}
