-- rls_role_setup.sql
-- Already applied to the database. Kept here for versioning/documentation.

-- Create a dedicated application role for RLS enforcement
-- This role will NOT be a superuser, so RLS policies apply to it
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


