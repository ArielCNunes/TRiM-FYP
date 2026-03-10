-- This script runs automatically when the Docker PostgreSQL container is first created.
-- It creates the non-superuser application role and sets up RLS policies.

-- 1. Create the application role (non-superuser, so RLS is enforced)
DO $$
BEGIN
      IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'trim_app_user') THEN
CREATE ROLE trim_app_user WITH LOGIN PASSWORD 'trim_app_password';
END IF;
END
  $$;

  -- Grant connect to the database
  GRANT CONNECT ON DATABASE barbershop_db TO trim_app_user;

  -- Grant usage on public schema
  GRANT USAGE ON SCHEMA public TO trim_app_user;

  -- Grant DML privileges on all existing tables
  GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO trim_app_user;

-- Grant usage on all sequences (needed for auto-increment IDs)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO trim_app_user;

-- Ensure future tables/sequences also get these grants
ALTER DEFAULT PRIVILEGES IN SCHEMA public
      GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO trim_app_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
      GRANT USAGE, SELECT ON SEQUENCES TO trim_app_user;

-- 2. Set default for the RLS session variable so current_setting doesn't error when unset
ALTER DATABASE barbershop_db SET app.current_business_id = '0';

  -- 3. Enable RLS on all tenant-scoped tables
  -- Sentinel values:
  --   '0'  — database default, matches no real business → sees nothing (safe)
  --   '-1' — explicit bypass set by RlsBypass → sees all rows (intentional)
  --   '1', '2', … — normal tenant IDs → sees own data only

  -- USERS
ALTER TABLE IF EXISTS users ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS users FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_users ON users
          USING (business_id = current_setting('app.current_business_id')::bigint
                 OR current_setting('app.current_business_id') = '-1')
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                      OR current_setting('app.current_business_id') = '-1');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- BARBERS
ALTER TABLE IF EXISTS barbers ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS barbers FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_barbers ON barbers
          USING (business_id = current_setting('app.current_business_id')::bigint
                 OR current_setting('app.current_business_id') = '-1')
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                      OR current_setting('app.current_business_id') = '-1');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- BOOKINGS
ALTER TABLE IF EXISTS bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS bookings FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_bookings ON bookings
          USING (business_id = current_setting('app.current_business_id')::bigint
                 OR current_setting('app.current_business_id') = '-1')
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                      OR current_setting('app.current_business_id') = '-1');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- BARBER_AVAILABILITY
ALTER TABLE IF EXISTS barber_availability ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS barber_availability FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_barber_availability ON barber_availability
          USING (business_id = current_setting('app.current_business_id')::bigint
                 OR current_setting('app.current_business_id') = '-1')
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                      OR current_setting('app.current_business_id') = '-1');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- BARBER_BREAKS
ALTER TABLE IF EXISTS barber_breaks ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS barber_breaks FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_barber_breaks ON barber_breaks
          USING (business_id = current_setting('app.current_business_id')::bigint
                 OR current_setting('app.current_business_id') = '-1')
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                      OR current_setting('app.current_business_id') = '-1');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- PAYMENTS
ALTER TABLE IF EXISTS payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS payments FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_payments ON payments
          USING (business_id = current_setting('app.current_business_id')::bigint
                 OR current_setting('app.current_business_id') = '-1')
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                      OR current_setting('app.current_business_id') = '-1');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- SERVICES_OFFERED
ALTER TABLE IF EXISTS services_offered ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS services_offered FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_services_offered ON services_offered
          USING (business_id = current_setting('app.current_business_id')::bigint
                 OR current_setting('app.current_business_id') = '-1')
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                      OR current_setting('app.current_business_id') = '-1');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- SERVICE_CATEGORIES
ALTER TABLE IF EXISTS service_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS service_categories FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_service_categories ON service_categories
          USING (business_id = current_setting('app.current_business_id')::bigint
                 OR current_setting('app.current_business_id') = '-1')
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint
                      OR current_setting('app.current_business_id') = '-1');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;