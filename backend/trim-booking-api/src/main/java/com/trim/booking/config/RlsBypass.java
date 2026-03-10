package com.trim.booking.config;

import com.trim.booking.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Utility for executing operations that need to bypass Row-Level Security.
 *
 * This temporarily sets app.current_business_id to '-1' (the bypass sentinel),
 * then restores the original tenant value after the operation completes.
 *
 * Sentinel values:
 *   '0'  — database default, matches no real business → sees nothing (safe)
 *   '-1' — explicit bypass, RLS policies grant access to all rows (intentional)
 *   '1', '2', … — normal tenant IDs → sees own data only
 *
 * Must be called within an active @Transactional context: SET LOCAL
 * only persists for the duration of a transaction.
 */
@Component
public class RlsBypass {

    private static final String BYPASS_SENTINEL = "-1";

    @Autowired
    private EntityManager entityManager;

    /**
     * Execute a callback with RLS bypassed (all business rows visible).
     * Must be called within an active @Transactional context.
     *
     * @param action The operation to run without tenant filtering
     * @param <T>    Return type
     * @return The result of the action
     */
    public <T> T executeWithoutRls(Supplier<T> action) {
        // Set to bypass sentinel — '-1' is never a real business_id
        entityManager.createNativeQuery("SELECT set_config('app.current_business_id', ?1, true)")
                .setParameter(1, BYPASS_SENTINEL)
                .getSingleResult();
        try {
            return action.get();
        } finally {
            // Restore real tenant value if one was set before the bypass
            Long businessId = TenantContext.getCurrentBusinessId();
            if (businessId != null) {
                entityManager.createNativeQuery(
                        "SELECT set_config('app.current_business_id', ?1, true)"
                ).setParameter(1, String.valueOf(businessId))
                .getSingleResult();
            }
        }
    }

    /**
     * Void variant for operations that don't return a value.
     */
    public void runWithoutRls(Runnable action) {
        executeWithoutRls(() -> {
            action.run();
            return null;
        });
    }
}

