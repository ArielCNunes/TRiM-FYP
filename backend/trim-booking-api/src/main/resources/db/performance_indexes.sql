-- Performance optimization indexes for booking system
-- Created: November 3, 2025
-- Purpose: Support optimized overlap queries and improve query performance

-- Index for efficient overlap checking in booking creation
-- Supports: hasOverlappingBookings query
-- Query pattern: WHERE barber_id = ? AND booking_date = ? AND start_time < ? AND end_time > ?
CREATE INDEX IF NOT EXISTS idx_bookings_barber_date_time
ON bookings(barber_id, booking_date, start_time, end_time);

-- Index for status filtering (used in many queries)
-- Supports: findByStatus, queries filtering cancelled bookings
CREATE INDEX IF NOT EXISTS idx_bookings_status
ON bookings(status);

-- Index for customer queries
-- Supports: findByCustomerId
CREATE INDEX IF NOT EXISTS idx_bookings_customer_id
ON bookings(customer_id);

-- Index for booking date queries
-- Supports: findByBookingDate, dashboard queries, reminder service
CREATE INDEX IF NOT EXISTS idx_bookings_booking_date
ON bookings(booking_date);

-- Index for payment status queries
-- Supports: revenue calculations, payment filtering
CREATE INDEX IF NOT EXISTS idx_bookings_payment_status
ON bookings(payment_status);

-- Composite index for barber schedule queries
-- Supports: findByBarberIdAndBookingDate, getBarberScheduleForDate
-- Note: Partially overlaps with idx_bookings_barber_date_time, but optimized for different access patterns
CREATE INDEX IF NOT EXISTS idx_bookings_barber_date_status
ON bookings(barber_id, booking_date, status);

-- Index for date-based dashboard and reminder queries
-- Supports: countTodaysBookings, countUpcomingBookings, date range queries
CREATE INDEX IF NOT EXISTS idx_bookings_date_status
ON bookings(booking_date, status);

-- Performance notes:
-- 1. These indexes significantly improve query performance for booking conflict checks
-- 2. The barber_date_time index is critical for the new optimized overlap query
-- 3. Indexes add minimal write overhead but dramatically improve read performance
-- 4. Consider monitoring index usage with:
--    SELECT * FROM pg_stat_user_indexes WHERE schemaname = 'public';
--    (PostgreSQL) or equivalent for your database

-- Maintenance:
-- Run ANALYZE bookings; after creating indexes to update query planner statistics
-- Monitor index size with: SELECT pg_size_pretty(pg_relation_size('index_name'));

