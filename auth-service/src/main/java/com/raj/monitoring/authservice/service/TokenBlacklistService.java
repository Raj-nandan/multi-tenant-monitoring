package com.raj.monitoring.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "blacklist:token:";

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Adds a token to the blacklist with its remaining TTL
     * @param token the JWT token to blacklist
     * @param expiryTimeSeconds the remaining validity time of the token
     */
    public void blacklistToken(String token, long expiryTimeSeconds) {
        String key = BLACKLIST_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", expiryTimeSeconds, TimeUnit.SECONDS);
        log.info("Token blacklisted for {} seconds", expiryTimeSeconds);
    }

    /**
     * Checks if a token is blacklisted
     * @param token the JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Gets the remaining TTL for a blacklisted token
     * @param token the JWT token
     * @return remaining time in seconds, or -1 if not found
     */
    public long getRemainingTTL(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -1;
    }

    /**
     * Manually removes a token from blacklist (if needed)
     * @param token the JWT token to remove
     */
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;
        redisTemplate.delete(key);
        log.info("Token removed from blacklist");
    }
}
