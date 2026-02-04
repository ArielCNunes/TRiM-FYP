package com.trim.booking.service.auth;

import com.trim.booking.entity.AuthTokenExchange;
import com.trim.booking.exception.BadRequestException;
import com.trim.booking.repository.AuthTokenExchangeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for secure cross-subdomain authentication.
 * Creates short-lived exchange tokens that can be traded for actual JWTs.
 */
@Service
public class AuthTokenExchangeService {

    private static final int TOKEN_EXPIRY_SECONDS = 60;
    private static final SecureRandom secureRandom = new SecureRandom();

    private final AuthTokenExchangeRepository repository;

    public AuthTokenExchangeService(AuthTokenExchangeRepository repository) {
        this.repository = repository;
    }

    /**
     * Create a short-lived exchange token for cross-subdomain redirect.
     *
     * @param jwtToken     The actual JWT to be retrieved later
     * @param userData     User data as JSON string
     * @param businessSlug Target business subdomain
     * @return The exchange token (safe to put in URL)
     */
    @Transactional
    public String createExchangeToken(String jwtToken, String userData, String businessSlug) {
        // Generate cryptographically secure random token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String exchangeToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        AuthTokenExchange exchange = new AuthTokenExchange();
        exchange.setExchangeToken(exchangeToken);
        exchange.setJwtToken(jwtToken);
        exchange.setUserData(userData);
        exchange.setBusinessSlug(businessSlug);
        exchange.setExpiresAt(LocalDateTime.now().plusSeconds(TOKEN_EXPIRY_SECONDS));
        exchange.setUsed(false);

        repository.save(exchange);

        return exchangeToken;
    }

    /**
     * Exchange a temporary token for the actual JWT and user data.
     * Token is invalidated after use (single-use).
     *
     * @param exchangeToken The temporary token from URL
     * @return Map containing "token" (JWT) and "user" (user data JSON)
     * @throws BadRequestException if token is invalid, expired, or already used
     */
    @Transactional
    public Map<String, String> exchangeToken(String exchangeToken) {
        AuthTokenExchange exchange = repository
                .findValidToken(exchangeToken, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Invalid or expired exchange token"));

        // Mark as used immediately (single-use)
        exchange.setUsed(true);
        repository.save(exchange);

        Map<String, String> result = new HashMap<>();
        result.put("token", exchange.getJwtToken());
        result.put("user", exchange.getUserData());
        result.put("businessSlug", exchange.getBusinessSlug());

        return result;
    }

    /**
     * Clean up expired and used tokens every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void cleanupExpiredTokens() {
        repository.deleteExpiredTokens(LocalDateTime.now());
    }
}
