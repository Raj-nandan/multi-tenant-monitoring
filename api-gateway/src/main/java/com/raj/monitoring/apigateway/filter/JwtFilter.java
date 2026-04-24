package com.raj.monitoring.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Configuration
@Slf4j
public class JwtFilter {

    private final StringRedisTemplate redisTemplate;
    private final SecretKey secretKey;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public JwtFilter(
            StringRedisTemplate redisTemplate,
            @Value("${jwt.secret}") String secret
    ) {
        this.redisTemplate = redisTemplate;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static final List<String> PUBLIC_ROUTES = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    @Bean
    public GlobalFilter jwtAuthFilter() {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();

            if (isPublic(path)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange, "Missing Bearer token");
            }

            String token = authHeader.substring(7);

            try {
                // 1. Check Redis blacklist
                String redisKey = "blacklist:token:" + token;
                Boolean blacklisted = redisTemplate.hasKey(redisKey);

                if (Boolean.TRUE.equals(blacklisted)) {
                    log.warn("Blocked blacklisted token");
                    return unauthorized(exchange, "Token has been revoked");
                }

                // 2. Validate JWT
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                if (claims.getExpiration().before(new Date())) {
                    return unauthorized(exchange, "Token expired");
                }

                // 3. Forward username as header
                String username = claims.getSubject();

                ServerWebExchange mutated = exchange.mutate()
                        .request(r -> r.header("X-Auth-User", username))
                        .build();

                return chain.filter(mutated);

            } catch (Exception ex) {
                log.error("JWT validation failed: {}", ex.getMessage());
                return unauthorized(exchange, "Invalid token");
            }
        };
    }

    private boolean isPublic(String path) {
        return PUBLIC_ROUTES.stream()
                .anyMatch(route -> matcher.match(route, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        String body = """
            {
              "success": false,
              "message": "%s"
            }
            """.formatted(message);

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(body.getBytes(StandardCharsets.UTF_8)))
        );
    }
}