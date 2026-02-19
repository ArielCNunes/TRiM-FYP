-- rls_setup.sql
-- Enable Row-Level Security (RLS) for multi-tenant isolation using app.current_business_id
-- This script has already been applied to the database. Kept here for versioning/documentation.
--
-- Tables with business_id: users, barbers, bookings, barber_availability,
--   barber_breaks, payments, services_offered, service_categories

-- Helper: set a default so current_setting doesn't error when unset
ALTER DATABASE barbershop_db SET app.current_business_id = '0';

-- 1. USERS TABLE
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE users FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_users ON users
    USING (business_id = current_setting('app.current_business_id')::bigint)
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);

-- 2. BARBERS TABLE
ALTER TABLE barbers ENABLE ROW LEVEL SECURITY;
ALTER TABLE barbers FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_barbers ON barbers
    USING (business_id = current_setting('app.current_business_id')::bigint)
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);

-- 3. BOOKINGS TABLE
ALTER TABLE bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookings FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_bookings ON bookings
    USING (business_id = current_setting('app.current_business_id')::bigint)
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);

-- 4. BARBER_AVAILABILITY TABLE
ALTER TABLE barber_availability ENABLE ROW LEVEL SECURITY;
ALTER TABLE barber_availability FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_barber_availability ON barber_availability
    USING (business_id = current_setting('app.current_business_id')::bigint)
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);

-- 5. BARBER_BREAKS TABLE
ALTER TABLE barber_breaks ENABLE ROW LEVEL SECURITY;
ALTER TABLE barber_breaks FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_barber_breaks ON barber_breaks
    USING (business_id = current_setting('app.current_business_id')::bigint)
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);

-- 6. PAYMENTS TABLE
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_payments ON payments
    USING (business_id = current_setting('app.current_business_id')::bigint)
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);

-- 7. SERVICES_OFFERED TABLE
ALTER TABLE services_offered ENABLE ROW LEVEL SECURITY;
ALTER TABLE services_offered FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_services_offered ON services_offered
    USING (business_id = current_setting('app.current_business_id')::bigint)
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);

-- 8. SERVICE_CATEGORIES TABLE
ALTER TABLE service_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_categories FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_service_categories ON service_categories
    USING (business_id = current_setting('app.current_business_id')::bigint)
    WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);

