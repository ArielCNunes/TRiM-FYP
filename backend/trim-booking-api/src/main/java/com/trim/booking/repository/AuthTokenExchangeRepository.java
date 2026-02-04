package com.trim.booking.repository;

import com.trim.booking.entity.AuthTokenExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AuthTokenExchangeRepository extends JpaRepository<AuthTokenExchange, Long> {

    /**
     * Find a valid (unused, not expired) exchange token.
     */
    @Query("SELECT t FROM AuthTokenExchange t WHERE t.exchangeToken = :token " +
           "AND t.used = false AND t.expiresAt > :now")
    Optional<AuthTokenExchange> findValidToken(String token, LocalDateTime now);

    /**
     * Clean up expired tokens (run periodically).
     */
    @Modifying
    @Query("DELETE FROM AuthTokenExchange t WHERE t.expiresAt < :now OR t.used = true")
    void deleteExpiredTokens(LocalDateTime now);
}
