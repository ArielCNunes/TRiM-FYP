GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO trim_app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO trim_app_user;

-- USERS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE users FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_users ON users
          USING (business_id = current_setting('app.current_business_id')::bigint)
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- BARBERS
ALTER TABLE barbers ENABLE ROW LEVEL SECURITY;
ALTER TABLE barbers FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_barbers ON barbers
          USING (business_id = current_setting('app.current_business_id')::bigint)
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- BOOKINGS
ALTER TABLE bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookings FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_bookings ON bookings
          USING (business_id = current_setting('app.current_business_id')::bigint)
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- BARBER_AVAILABILITY
ALTER TABLE barber_availability ENABLE ROW LEVEL SECURITY;
ALTER TABLE barber_availability FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_barber_availability ON barber_availability
          USING (business_id = current_setting('app.current_business_id')::bigint)
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- BARBER_BREAKS
ALTER TABLE barber_breaks ENABLE ROW LEVEL SECURITY;
ALTER TABLE barber_breaks FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_barber_breaks ON barber_breaks
          USING (business_id = current_setting('app.current_business_id')::bigint)
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- PAYMENTS
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_payments ON payments
          USING (business_id = current_setting('app.current_business_id')::bigint)
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- SERVICES_OFFERED
ALTER TABLE services_offered ENABLE ROW LEVEL SECURITY;
ALTER TABLE services_offered FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_services_offered ON services_offered
          USING (business_id = current_setting('app.current_business_id')::bigint)
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

  -- SERVICE_CATEGORIES
ALTER TABLE service_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_categories FORCE ROW LEVEL SECURITY;
DO $$ BEGIN
      CREATE POLICY tenant_isolation_service_categories ON service_categories
          USING (business_id = current_setting('app.current_business_id')::bigint)
          WITH CHECK (business_id = current_setting('app.current_business_id')::bigint);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;