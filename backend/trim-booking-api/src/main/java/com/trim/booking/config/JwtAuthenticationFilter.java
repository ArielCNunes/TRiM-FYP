package com.trim.booking.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filter that intercepts every request to validate JWT tokens.
 * <p>
 * Runs once per request, extracts JWT from Authorization header,
 * validates it, and sets authentication in SecurityContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Extract Authorization header
        String authHeader = request.getHeader("Authorization");

        // Step 2: Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token, continue to next filter (will be rejected by SecurityConfig)
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        try {
            // Step 4: Validate token
            if (jwtUtil.isTokenValid(token)) {
                // Step 5: Extract user details from token
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);
                Long userId = jwtUtil.extractUserId(token);

                // Step 6: Create authentication object
                // Grant authority based on role (e.g., ROLE_CUSTOMER)
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,  // Principal (who is authenticated)
                                null,   // Credentials (not needed after authentication)
                                Collections.singletonList(authority)  // Authorities (permissions)
                        );

                // Add userId to details so controllers can access it
                authentication.setDetails(userId);

                // Step 7: Set authentication in SecurityContext
                // Now Spring Security knows this user is authenticated
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Token validation failed - continue without authentication
            // SecurityConfig will reject the request
            System.out.println("JWT validation failed: " + e.getMessage());
        }

        // Step 8: Continue to next filter
        filterChain.doFilter(request, response);
    }
}