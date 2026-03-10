-- fix-rls-bypass.sql
-- Migration: Add bypass condition to RLS policies so that setting
-- app.current_business_id = '-1' allows access to all rows.
-- This fixes the Stripe webhook "Payment not found" issue where
-- RlsBypass sets the config to '-1' but the policies only matched
-- exact business_id equality, returning no rows.
--
-- Sentinel values:
--   '0'  — database default, matches no real business → sees nothing (safe)
--   '-1' — explicit bypass set by RlsBypass → sees all rows (intentional)
--   '1', '2', … — normal tenant IDs → sees own data only
--
-- Run this script against the live database to update existing policies.

-- Drop and recreate each policy with the bypass condition.

-- 1. USERS
DROP POLICY IF EXISTS tenant_isolation_users ON users;
CREATE POLICY tenant_isolation_users ON users
    USING (business_id = current_setting('app.current_business_id')::bigint
           OR current_setting('app.current_business_id') = '-1')
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                OR current_setting('app.current_business_id') = '-1');

-- 2. BARBERS
DROP POLICY IF EXISTS tenant_isolation_barbers ON barbers;
CREATE POLICY tenant_isolation_barbers ON barbers
    USING (business_id = current_setting('app.current_business_id')::bigint
           OR current_setting('app.current_business_id') = '-1')
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                OR current_setting('app.current_business_id') = '-1');

-- 3. BOOKINGS
DROP POLICY IF EXISTS tenant_isolation_bookings ON bookings;
CREATE POLICY tenant_isolation_bookings ON bookings
    USING (business_id = current_setting('app.current_business_id')::bigint
           OR current_setting('app.current_business_id') = '-1')
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                OR current_setting('app.current_business_id') = '-1');

-- 4. BARBER_AVAILABILITY
DROP POLICY IF EXISTS tenant_isolation_barber_availability ON barber_availability;
CREATE POLICY tenant_isolation_barber_availability ON barber_availability
    USING (business_id = current_setting('app.current_business_id')::bigint
           OR current_setting('app.current_business_id') = '-1')
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                OR current_setting('app.current_business_id') = '-1');

-- 5. BARBER_BREAKS
DROP POLICY IF EXISTS tenant_isolation_barber_breaks ON barber_breaks;
CREATE POLICY tenant_isolation_barber_breaks ON barber_breaks
    USING (business_id = current_setting('app.current_business_id')::bigint
           OR current_setting('app.current_business_id') = '-1')
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                OR current_setting('app.current_business_id') = '-1');

-- 6. PAYMENTS
DROP POLICY IF EXISTS tenant_isolation_payments ON payments;
CREATE POLICY tenant_isolation_payments ON payments
    USING (business_id = current_setting('app.current_business_id')::bigint
           OR current_setting('app.current_business_id') = '-1')
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                OR current_setting('app.current_business_id') = '-1');

-- 7. SERVICES_OFFERED
DROP POLICY IF EXISTS tenant_isolation_services_offered ON services_offered;
CREATE POLICY tenant_isolation_services_offered ON services_offered
    USING (business_id = current_setting('app.current_business_id')::bigint
           OR current_setting('app.current_business_id') = '-1')
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                OR current_setting('app.current_business_id') = '-1');

-- 8. SERVICE_CATEGORIES
DROP POLICY IF EXISTS tenant_isolation_service_categories ON service_categories;
CREATE POLICY tenant_isolation_service_categories ON service_categories
    USING (business_id = current_setting('app.current_business_id')::bigint
           OR current_setting('app.current_business_id') = '-1')
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                OR current_setting('app.current_business_id') = '-1');

