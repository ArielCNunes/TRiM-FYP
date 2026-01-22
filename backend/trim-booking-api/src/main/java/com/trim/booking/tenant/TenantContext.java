package com.trim.booking.tenant;

/**
 * Thread-local storage for tenant context in multi-tenant application.
 * Stores the current business ID and slug for the duration of a request.
 */
public class TenantContext {

    private static final ThreadLocal<Long> currentBusinessId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentBusinessSlug = new ThreadLocal<>();

    private TenantContext() {
        // Utility class, prevent instantiation
    }

    /**
     * Set the current business context for this thread/request.
     *
     * @param id   the business ID
     * @param slug the business slug
     */
    public static void setCurrentBusiness(Long id, String slug) {
        currentBusinessId.set(id);
        currentBusinessSlug.set(slug);
    }

    /**
     * Get the current business ID for this thread/request.
     *
     * @return the current business ID, or null if not set
     */
    public static Long getCurrentBusinessId() {
        return currentBusinessId.get();
    }

    /**
     * Get the current business slug for this thread/request.
     *
     * @return the current business slug, or null if not set
     */
    public static String getCurrentBusinessSlug() {
        return currentBusinessSlug.get();
    }

    /**
     * Clear the tenant context. Should be called in a finally block
     * after request processing to prevent memory leaks.
     */
    public static void clear() {
        currentBusinessId.remove();
        currentBusinessSlug.remove();
    }

    /**
     * Check if a tenant context has been set for this thread/request.
     *
     * @return true if tenant context is set, false otherwise
     */
    public static boolean isSet() {
        return currentBusinessId.get() != null;
    }
}
