package com.trim.booking.tenant;

import com.trim.booking.entity.Business;
import com.trim.booking.repository.BusinessRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * Filter that extracts tenant (business) information from incoming requests
 * and sets it in the TenantContext for use throughout the request lifecycle.
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    private final BusinessRepository businessRepository;

    /**
     * Endpoints that don't require tenant resolution.
     */
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/auth/register-admin",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/validate-reset-token",
            "/api/payments/webhook",
            "/swagger-ui",
            "/v3/api-docs"
    );

    /**
     * Subdomains that should be ignored (not treated as business slugs).
     */
    private static final Set<String> IGNORED_SUBDOMAINS = Set.of(
            "localhost",
            "www",
            "api",
            "admin",
            "app"
    );

    public TenantFilter(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Skip tenant resolution for public endpoints
            if (!shouldResolveTenant(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Try to extract business slug from various sources
            String slug = extractBusinessSlug(request);

            // Reject if no slug provided
            if (slug == null || slug.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Business context required. Please provide a valid business slug.\"}");
                return;
            }

            // Look up the business
            Optional<Business> business = businessRepository.findBySlug(slug);

            // Reject if business not found
            if (business.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Business not found for slug: " + slug + "\"}");
                return;
            }

            // Set tenant context and proceed
            TenantContext.setCurrentBusiness(business.get().getId(), slug);
            filterChain.doFilter(request, response);
        } finally {
            // Always clear the context to prevent memory leaks
            TenantContext.clear();
        }
    }

    /**
     * Determine if tenant resolution should be performed for this request.
     */
    private boolean shouldResolveTenant(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Check if path starts with any public endpoint
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (path.startsWith(publicEndpoint)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Extract business slug from the request using multiple strategies:
     * 1. Subdomain from Host header
     * 2. X-Business-Slug header
     * 3. "business" query parameter
     */
    private String extractBusinessSlug(HttpServletRequest request) {
        // Strategy 1: Extract from subdomain
        String slug = extractFromSubdomain(request);
        if (slug != null && !slug.isEmpty()) {
            return slug;
        }

        // Strategy 2: X-Business-Slug header
        slug = request.getHeader("X-Business-Slug");
        if (slug != null && !slug.isEmpty()) {
            return slug;
        }

        // Strategy 3: Query parameter
        slug = request.getParameter("business");
        if (slug != null && !slug.isEmpty()) {
            return slug;
        }

        return null;
    }

    /**
     * Extract business slug from subdomain.
     * Example: "v7.localhost:3000" → "v7"
     * Example: "shop2.trim.com" → "shop2"
     */
    private String extractFromSubdomain(HttpServletRequest request) {
        String host = request.getHeader("Host");
        if (host == null || host.isEmpty()) {
            return null;
        }

        // Remove port if present (e.g., "v7.localhost:3000" → "v7.localhost")
        String hostname = host.split(":")[0];

        // Get first part before the first dot
        String[] parts = hostname.split("\\.");
        if (parts.length < 2) {
            // No subdomain present (e.g., just "localhost")
            return null;
        }

        String subdomain = parts[0].toLowerCase();

        // Ignore common non-business subdomains
        if (IGNORED_SUBDOMAINS.contains(subdomain)) {
            return null;
        }

        return subdomain;
    }
}
