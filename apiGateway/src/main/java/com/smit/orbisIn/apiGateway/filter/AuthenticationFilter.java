package com.smit.orbisIn.apiGateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthenticationFilter.class);

    private static final String USER_ID_HEADER = "X-User-Id";

    private static final String AUTHORIZATION_HEADER =
            HttpHeaders.AUTHORIZATION;

    private static final String BEARER_PREFIX =
            "Bearer ";

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    /**
     * Creates the signing key from the configured JWT secret.
     */
    private SecretKey getSecretKey() {

        return Keys.hmacShaKeyFor(
                jwtSecretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Validates the JWT and extracts the user ID
     * from the JWT subject.
     */
    private Long validateAndGetUserId(String token) {

        var claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {

        ServerHttpRequest request = exchange.getRequest();

        String authHeader = request
                .getHeaders()
                .getFirst(AUTHORIZATION_HEADER);

        /*
         * No Authorization header.
         *
         * Public endpoints such as:
         *
         * POST /auth/signup
         * POST /auth/login
         *
         * can continue without authentication.
         */
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {

            return chain.filter(exchange);
        }

        /*
         * Extract JWT.
         */
        String token = authHeader.substring(
                BEARER_PREFIX.length()
        );

        try {

            /*
             * Validate JWT and extract user ID.
             */
            Long userId = validateAndGetUserId(token);

            logger.debug(
                    "JWT validated successfully for userId: {}",
                    userId
            );

            /*
             * Add the authenticated user ID
             * to the downstream request.
             */
            ServerHttpRequest mutatedRequest = request
                    .mutate()
                    .header(
                            USER_ID_HEADER,
                            userId.toString()
                    )
                    .build();

            /*
             * Create a new exchange containing
             * the modified request.
             */
            ServerWebExchange mutatedExchange = exchange
                    .mutate()
                    .request(mutatedRequest)
                    .build();

            /*
             * Continue to the next filter / downstream service.
             */
            return chain.filter(mutatedExchange);

        } catch (Exception e) {

            logger.warn(
                    "Invalid JWT token received: {}",
                    e.getMessage()
            );

            /*
             * Invalid or expired JWT.
             */
            exchange.getResponse().setStatusCode(
                    HttpStatus.UNAUTHORIZED
            );

            return exchange
                    .getResponse()
                    .setComplete();
        }
    }

    @Override
    public int getOrder() {

        return Ordered.HIGHEST_PRECEDENCE;
    }
}