package com.trim.booking.seeder;

import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JDBC Batch Data Seeder for Performance Testing
 * This seeder populates the database with realistic test data for benchmarking.
 * It uses raw JDBC batch inserts for maximum performance.
 */
@Component
@Profile("seed")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final Faker faker;

    // Pre-computed BCrypt hash for "password123"
    private static final String PASSWORD_HASH;
    static {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        PASSWORD_HASH = encoder.encode("password123");
    }

    // Configurable parameters with defaults
    @Value("${seed.businesses:5}")
    private int numBusinesses;

    @Value("${seed.customers-per-business:50000}")
    private int customersPerBusiness;

    @Value("${seed.bookings-per-business:100000}")
    private int bookingsPerBusiness;

    @Value("${seed.barbers-per-business:10}")
    private int barbersPerBusiness;

    @Value("${seed.categories-per-business:5}")
    private int categoriesPerBusiness;

    @Value("${seed.services-per-category:4}")
    private int servicesPerCategory;

    @Value("${seed.batch-size:1000}")
    private int batchSize;

    // Phone number generator (globally unique)
    private final AtomicLong phoneCounter = new AtomicLong(3530000000000L);

    // Service categories and names for realistic data
    private static final String[] CATEGORY_NAMES = {
        "Haircuts", "Beard & Shave", "Hair Treatments", "Coloring", "Styling"
    };

    private static final Map<String, String[]> SERVICES_BY_CATEGORY = Map.of(
        "Haircuts", new String[]{"Classic Cut", "Fade", "Buzz Cut", "Scissor Cut"},
        "Beard & Shave", new String[]{"Beard Trim", "Hot Towel Shave", "Beard Shape", "Goatee Trim"},
        "Hair Treatments", new String[]{"Deep Conditioning", "Scalp Treatment", "Keratin Treatment", "Hair Mask"},
        "Coloring", new String[]{"Full Color", "Highlights", "Lowlights", "Gray Blending"},
        "Styling", new String[]{"Blow Dry", "Hair Wax Style", "Pomade Style", "Special Event Style"}
    );

    public DataSeeder(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.faker = new Faker();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=".repeat(80));
        log.info("STARTING DATA SEEDER FOR PERFORMANCE TESTING");
        log.info("=".repeat(80));
        log.info("Configuration:");
        log.info("  - Businesses: {}", numBusinesses);
        log.info("  - Barbers per business: {}", barbersPerBusiness);
        log.info("  - Customers per business: {}", customersPerBusiness);
        log.info("  - Bookings per business: {}", bookingsPerBusiness);
        log.info("  - Categories per business: {}", categoriesPerBusiness);
        log.info("  - Services per category: {}", servicesPerCategory);
        log.info("  - Batch size: {}", batchSize);
        log.info("=".repeat(80));

        long startTime = System.currentTimeMillis();

        // Truncate all tables first
        truncateAllTables();

        // Seed each business in its own transaction
        for (int i = 0; i < numBusinesses; i++) {
            final int businessIndex = i + 1;
            seedBusiness(businessIndex);
        }

        long duration = (System.currentTimeMillis() - startTime) / 1000;
        log.info("=".repeat(80));
        log.info("DATA SEEDING COMPLETED in {} seconds", duration);
        log.info("=".repeat(80));
    }

    /**
     * Truncate all tables in reverse FK order with CASCADE
     */
    private void truncateAllTables() {
        log.info("Truncating all tables...");

        // Order matters - children before parents, or use CASCADE
        String[] tables = {
            "payments",
            "bookings",
            "barber_breaks",
            "barber_availability",
            "services_offered",
            "service_categories",
            "barbers",
            "users",
            "businesses"
        };

        for (String table : tables) {
            try {
                jdbcTemplate.execute("TRUNCATE TABLE " + table + " RESTART IDENTITY CASCADE");
                log.info("  Truncated: {}", table);
            } catch (Exception e) {
                log.warn("  Could not truncate {}: {}", table, e.getMessage());
            }
        }
        log.info("Truncation complete.");
    }

    /**
     * Seed all data for a single business using a single connection
     */
    private void seedBusiness(int businessIndex) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String businessName = "Business " + businessIndex;
                String businessSlug = "business-" + businessIndex;

                log.info("-".repeat(60));
                log.info("Seeding business {}/{}: {}", businessIndex, numBusinesses, businessName);
                log.info("-".repeat(60));

                // 1. Create Business (without admin_user_id initially)
                Long businessId = insertBusiness(conn, businessName, businessSlug);
                log.info("  Created business ID: {}", businessId);

                // 2. Create Admin User
                Long adminUserId = insertUser(conn,
                    "Admin", "User",
                    "admin@" + businessSlug + ".test",
                    generateUniquePhone(),
                    "ADMIN",
                    businessId
                );
                log.info("  Created admin user ID: {}", adminUserId);

                // Update business with admin_user_id
                try (PreparedStatement ps = conn.prepareStatement("UPDATE businesses SET admin_user_id = ? WHERE id = ?")) {
                    ps.setLong(1, adminUserId);
                    ps.setLong(2, businessId);
                    ps.executeUpdate();
                }

                // 3. Create Barber Users and Barber entities
                List<Long> barberIds = new ArrayList<>();

                for (int i = 1; i <= barbersPerBusiness; i++) {
                    Long barberUserId = insertUser(conn,
                        faker.name().firstName(),
                        faker.name().lastName(),
                        "barber" + i + "@" + businessSlug + ".test",
                        generateUniquePhone(),
                        "BARBER",
                        businessId
                    );

                    Long barberId = insertBarber(conn, barberUserId, businessId);
                    barberIds.add(barberId);
                }
                log.info("  Created {} barbers", barbersPerBusiness);

                // 4. Create BarberAvailability (7 days per barber)
                insertBarberAvailabilities(conn, barberIds, businessId);
                log.info("  Created barber availabilities (7 per barber)");

                // 5. Create BarberBreaks (2 per barber)
                insertBarberBreaks(conn, barberIds, businessId);
                log.info("  Created barber breaks (2 per barber)");

                // 6. Create ServiceCategories and Services
                List<Long> allServiceIds = new ArrayList<>();
                List<BigDecimal> servicePrices = new ArrayList<>();
                List<Integer> serviceDeposits = new ArrayList<>();
                List<Integer> serviceDurations = new ArrayList<>();

                for (int i = 0; i < categoriesPerBusiness; i++) {
                    String categoryName = i < CATEGORY_NAMES.length ? CATEGORY_NAMES[i] : "Category " + (i + 1);
                    Long categoryId = insertServiceCategory(conn, categoryName, businessId);

                    String[] serviceNames = SERVICES_BY_CATEGORY.getOrDefault(categoryName,
                        new String[]{"Service A", "Service B", "Service C", "Service D"});

                    for (int j = 0; j < servicesPerCategory; j++) {
                        String serviceName = j < serviceNames.length ? serviceNames[j] : categoryName + " Service " + (j + 1);
                        int duration = 15 + (j * 15); // 15, 30, 45, 60 minutes
                        BigDecimal price = BigDecimal.valueOf(15 + (j * 10)).setScale(2, RoundingMode.HALF_UP); // €15-€45
                        int depositPercentage = 25 + (j * 5); // 25%, 30%, 35%, 40%

                        Long serviceId = insertService(conn, serviceName, duration, price, depositPercentage, categoryId, businessId);
                        allServiceIds.add(serviceId);
                        servicePrices.add(price);
                        serviceDeposits.add(depositPercentage);
                        serviceDurations.add(duration);
                    }
                }
                log.info("  Created {} categories with {} services each", categoriesPerBusiness, servicesPerCategory);

                // 7. Create Customer Users in batches
                List<Long> customerIds = insertCustomersBatch(conn, businessSlug, businessId);
                log.info("  Created {} customers", customerIds.size());

                // 8. Create Bookings in batches
                List<BookingData> bookingsData = insertBookingsBatch(conn,
                    customerIds, barberIds, allServiceIds,
                    servicePrices, serviceDeposits, serviceDurations, businessId
                );
                log.info("  Created {} bookings", bookingsData.size());

                // 9. Create Payments for CONFIRMED/COMPLETED bookings
                int paymentsCreated = insertPaymentsBatch(conn, bookingsData, businessId);
                log.info("  Created {} payments", paymentsCreated);

                conn.commit();
                log.info("Business {} seeding complete!", businessIndex);
            } catch (Exception e) {
                conn.rollback();
                log.error("Error seeding business {}: {}", businessIndex, e.getMessage(), e);
                throw new RuntimeException("Failed to seed business " + businessIndex, e);
            }
        } catch (SQLException e) {
            log.error("Failed to get connection for business {}: {}", businessIndex, e.getMessage(), e);
            throw new RuntimeException("Failed to seed business " + businessIndex, e);
        }
    }

    /**
     * Insert a business and return its generated ID
     */
    private Long insertBusiness(Connection conn, String name, String slug) throws SQLException {
        String sql = "INSERT INTO businesses (name, slug, created_at) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, slug);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to get generated ID for business");
    }

    /**
     * Insert a user and return its generated ID
     */
    private Long insertUser(Connection conn, String firstName, String lastName, String email,
                           String phone, String role, Long businessId) throws SQLException {
        String sql = """
            INSERT INTO users (first_name, last_name, email, password_hash, phone, role, 
                             created_at, blacklisted, business_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, PASSWORD_HASH);
            ps.setString(5, phone);
            ps.setString(6, role);
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBoolean(8, false);
            ps.setLong(9, businessId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to get generated ID for user");
    }

    /**
     * Insert a barber and return its generated ID
     */
    private Long insertBarber(Connection conn, Long userId, Long businessId) throws SQLException {
        String sql = "INSERT INTO barbers (user_id, bio, active, business_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, userId);
            ps.setString(2, faker.lorem().paragraph());
            ps.setBoolean(3, true);
            ps.setLong(4, businessId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to get generated ID for barber");
    }

    /**
     * Insert barber availabilities in batch
     */
    private void insertBarberAvailabilities(Connection conn, List<Long> barberIds, Long businessId) throws SQLException {
        String sql = """
            INSERT INTO barber_availability (barber_id, day_of_week, start_time, end_time, is_available, business_id) 
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            int count = 0;
            for (Long barberId : barberIds) {
                for (DayOfWeek day : DayOfWeek.values()) {
                    ps.setLong(1, barberId);
                    ps.setString(2, day.name());

                    if (day == DayOfWeek.SUNDAY) {
                        // Sunday - unavailable
                        ps.setTime(3, Time.valueOf(LocalTime.of(0, 0)));
                        ps.setTime(4, Time.valueOf(LocalTime.of(0, 0)));
                        ps.setBoolean(5, false);
                    } else if (day == DayOfWeek.SATURDAY) {
                        // Saturday 09:00-13:00
                        ps.setTime(3, Time.valueOf(LocalTime.of(9, 0)));
                        ps.setTime(4, Time.valueOf(LocalTime.of(13, 0)));
                        ps.setBoolean(5, true);
                    } else {
                        // Mon-Fri 09:00-17:00
                        ps.setTime(3, Time.valueOf(LocalTime.of(9, 0)));
                        ps.setTime(4, Time.valueOf(LocalTime.of(17, 0)));
                        ps.setBoolean(5, true);
                    }

                    ps.setLong(6, businessId);
                    ps.addBatch();

                    if (++count % batchSize == 0) {
                        ps.executeBatch();
                    }
                }
            }
            ps.executeBatch();
        }
    }

    /**
     * Insert barber breaks in batch
     */
    private void insertBarberBreaks(Connection conn, List<Long> barberIds, Long businessId) throws SQLException {
        String sql = """
            INSERT INTO barber_breaks (barber_id, start_time, end_time, label, business_id) 
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Long barberId : barberIds) {
                // Lunch break 12:00-12:30
                ps.setLong(1, barberId);
                ps.setTime(2, Time.valueOf(LocalTime.of(12, 0)));
                ps.setTime(3, Time.valueOf(LocalTime.of(12, 30)));
                ps.setString(4, "Lunch Break");
                ps.setLong(5, businessId);
                ps.addBatch();

                // Afternoon break 15:00-15:15
                ps.setLong(1, barberId);
                ps.setTime(2, Time.valueOf(LocalTime.of(15, 0)));
                ps.setTime(3, Time.valueOf(LocalTime.of(15, 15)));
                ps.setString(4, "Afternoon Break");
                ps.setLong(5, businessId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Insert a service category and return its generated ID
     */
    private Long insertServiceCategory(Connection conn, String name, Long businessId) throws SQLException {
        String sql = "INSERT INTO service_categories (name, active, business_id) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setBoolean(2, true);
            ps.setLong(3, businessId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to get generated ID for service category");
    }

    /**
     * Insert a service and return its generated ID
     */
    private Long insertService(Connection conn, String name, int duration, BigDecimal price,
                               int depositPercentage, Long categoryId, Long businessId) throws SQLException {
        String sql = """
            INSERT INTO services_offered (name, description, duration_minutes, price, 
                                         deposit_percentage, active, category_id, business_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, faker.lorem().sentence());
            ps.setInt(3, duration);
            ps.setBigDecimal(4, price);
            ps.setInt(5, depositPercentage);
            ps.setBoolean(6, true);
            ps.setLong(7, categoryId);
            ps.setLong(8, businessId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to get generated ID for service");
    }

    /**
     * Insert customers in batches and return their IDs
     */
    private List<Long> insertCustomersBatch(Connection conn, String businessSlug, Long businessId) throws SQLException {
        List<Long> customerIds = new ArrayList<>(customersPerBusiness);

        String sql = """
            INSERT INTO users (first_name, last_name, email, password_hash, phone, role, 
                             created_at, blacklisted, business_id) 
            VALUES (?, ?, ?, ?, ?, 'CUSTOMER', ?, false, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int batchCount = 0;
            int totalInserted = 0;

            for (int i = 1; i <= customersPerBusiness; i++) {
                ps.setString(1, faker.name().firstName());
                ps.setString(2, faker.name().lastName());
                ps.setString(3, "customer" + i + "@" + businessSlug + ".test");
                ps.setString(4, PASSWORD_HASH);
                ps.setString(5, generateUniquePhone());
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now().minusDays(faker.random().nextInt(365))));
                ps.setLong(7, businessId);
                ps.addBatch();
                batchCount++;

                if (batchCount >= batchSize) {
                    ps.executeBatch();

                    // Get generated IDs
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        while (rs.next()) {
                            customerIds.add(rs.getLong(1));
                        }
                    }

                    totalInserted += batchCount;
                    batchCount = 0;

                    if (totalInserted % 10000 == 0) {
                        log.info("    Inserted {}/{} customers...", totalInserted, customersPerBusiness);
                    }
                }
            }

            // Execute remaining batch
            if (batchCount > 0) {
                ps.executeBatch();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    while (rs.next()) {
                        customerIds.add(rs.getLong(1));
                    }
                }
            }
        }

        return customerIds;
    }

    /**
     * Insert bookings in batches and return booking data for payment creation
     */
    private List<BookingData> insertBookingsBatch(Connection conn, List<Long> customerIds, List<Long> barberIds,
                                                   List<Long> serviceIds, List<BigDecimal> servicePrices,
                                                   List<Integer> serviceDeposits, List<Integer> serviceDurations,
                                                   Long businessId) throws SQLException {

        List<BookingData> bookingsData = new ArrayList<>(bookingsPerBusiness);
        Random random = new Random();

        // Date range: last 12 months
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(12);
        int totalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);

        // Track booked slots per barber per day to prevent overlaps
        // Key: barberId + "_" + date, Value: list of booked time ranges
        Map<String, List<int[]>> barberDaySlots = new HashMap<>();

        String sql = """
            INSERT INTO bookings (customer_id, barber_id, service_id, booking_date, start_time, end_time,
                                 status, payment_status, deposit_amount, outstanding_balance, notes, 
                                 created_at, business_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int batchCount = 0;
            int totalInserted = 0;
            int skippedDueToOverlap = 0;

            // Define break times in minutes from midnight
            // Lunch break: 12:00-12:30 (720-750)
            // Afternoon break: 15:00-15:15 (900-915)
            final int LUNCH_START = 12 * 60;      // 720
            final int LUNCH_END = 12 * 60 + 30;   // 750
            final int AFTERNOON_START = 15 * 60;  // 900
            final int AFTERNOON_END = 15 * 60 + 15; // 915

            for (int i = 0; i < bookingsPerBusiness; i++) {
                // Random customer, barber, service
                Long customerId = customerIds.get(random.nextInt(customerIds.size()));
                Long barberId = barberIds.get(random.nextInt(barberIds.size()));
                int serviceIndex = random.nextInt(serviceIds.size());
                Long serviceId = serviceIds.get(serviceIndex);
                BigDecimal price = servicePrices.get(serviceIndex);
                int depositPercentage = serviceDeposits.get(serviceIndex);
                int duration = serviceDurations.get(serviceIndex);

                // Random date in the last 12 months, but skip Sundays (barbers unavailable)
                LocalDate bookingDate;
                do {
                    bookingDate = startDate.plusDays(random.nextInt(totalDays + 1));
                } while (bookingDate.getDayOfWeek() == DayOfWeek.SUNDAY);

                // Find a non-overlapping time slot for this barber on this day
                String slotKey = barberId + "_" + bookingDate;
                List<int[]> bookedSlots = barberDaySlots.computeIfAbsent(slotKey, k -> new ArrayList<>());

                // Determine working hours based on day of week
                // Saturday: 09:00-13:00, Mon-Fri: 09:00-17:00
                int workStartHour = 9;
                int workEndHour = bookingDate.getDayOfWeek() == DayOfWeek.SATURDAY ? 13 : 17;

                // Try to find a free slot in 15-min increments
                LocalTime startTime = null;
                LocalTime endTime = null;

                // Generate available slots and shuffle them
                List<Integer> availableStartMinutes = new ArrayList<>();
                for (int hour = workStartHour; hour < workEndHour; hour++) {
                    for (int minute = 0; minute < 60; minute += 15) {
                        int startMinuteOfDay = hour * 60 + minute;
                        int endMinuteOfDay = startMinuteOfDay + duration;
                        if (endMinuteOfDay <= workEndHour * 60) { // Ensure end time doesn't exceed working hours
                            availableStartMinutes.add(startMinuteOfDay);
                        }
                    }
                }
                Collections.shuffle(availableStartMinutes, random);

                for (int startMinuteOfDay : availableStartMinutes) {
                    int endMinuteOfDay = startMinuteOfDay + duration;

                    // Check if this slot overlaps with break times
                    // Lunch break: 12:00-12:30 (only applies if working past 12:00)
                    // Afternoon break: 15:00-15:15 (only applies on weekdays, not Saturdays)
                    boolean overlapsLunchBreak = !(endMinuteOfDay <= LUNCH_START || startMinuteOfDay >= LUNCH_END);
                    boolean overlapsAfternoonBreak = bookingDate.getDayOfWeek() != DayOfWeek.SATURDAY &&
                        !(endMinuteOfDay <= AFTERNOON_START || startMinuteOfDay >= AFTERNOON_END);

                    boolean overlapsBreak = overlapsLunchBreak || overlapsAfternoonBreak;

                    if (overlapsBreak) {
                        continue; // Skip this slot, it overlaps with a break
                    }

                    // Check if this slot overlaps with any existing booking
                    boolean overlaps = false;
                    for (int[] slot : bookedSlots) {
                        if (!(endMinuteOfDay <= slot[0] || startMinuteOfDay >= slot[1])) {
                            overlaps = true;
                            break;
                        }
                    }

                    if (!overlaps) {
                        startTime = LocalTime.of(startMinuteOfDay / 60, startMinuteOfDay % 60);
                        endTime = LocalTime.of(endMinuteOfDay / 60, endMinuteOfDay % 60);
                        bookedSlots.add(new int[]{startMinuteOfDay, endMinuteOfDay});
                        break;
                    }
                }

                // If no slot found, skip this booking
                if (startTime == null) {
                    skippedDueToOverlap++;
                    continue;
                }

                // Random status with weighted distribution (no PENDING)
                String status = getWeightedStatus(random);
                String paymentStatus = getPaymentStatusForBookingStatus(status, random);

                // Calculate deposit and outstanding balance using the same logic as DepositCalculationService
                BigDecimal[] amounts = calculateDepositAndBalance(price, depositPercentage);
                BigDecimal depositAmount = amounts[0];
                BigDecimal outstandingBalance = amounts[1];

                // Adjust based on status
                if (status.equals("COMPLETED")) {
                    paymentStatus = "FULLY_PAID";
                    outstandingBalance = BigDecimal.ZERO;
                } else if (status.equals("CANCELLED")) {
                    paymentStatus = "CANCELLED";
                } else if (status.equals("CONFIRMED") || status.equals("NO_SHOW")) {
                    paymentStatus = "DEPOSIT_PAID";
                }

                LocalDateTime createdAt = bookingDate.minusDays(random.nextInt(7) + 1).atTime(
                        random.nextInt(12) + 8, random.nextInt(60));

                ps.setLong(1, customerId);
                ps.setLong(2, barberId);
                ps.setLong(3, serviceId);
                ps.setDate(4, java.sql.Date.valueOf(bookingDate));
                ps.setTime(5, Time.valueOf(startTime));
                ps.setTime(6, Time.valueOf(endTime));
                ps.setString(7, status);
                ps.setString(8, paymentStatus);
                ps.setBigDecimal(9, depositAmount);
                ps.setBigDecimal(10, outstandingBalance);
                ps.setString(11, random.nextInt(10) == 0 ? faker.lorem().sentence() : null); // 10% have notes
                ps.setTimestamp(12, Timestamp.valueOf(createdAt));
                ps.setLong(13, businessId);
                ps.addBatch();
                batchCount++;

                if (batchCount >= batchSize) {
                    ps.executeBatch();

                    totalInserted += batchCount;
                    batchCount = 0;

                    if (totalInserted % 20000 == 0) {
                        log.info("    Inserted {}/{} bookings...", totalInserted, bookingsPerBusiness);
                    }
                }
            }

            // Execute remaining batch
            if (batchCount > 0) {
                ps.executeBatch();
                totalInserted += batchCount;
            }

            if (skippedDueToOverlap > 0) {
                log.info("    Skipped {} bookings due to time slot conflicts", skippedDueToOverlap);
            }
        }

        // Now retrieve all bookings that need payments (CONFIRMED or COMPLETED)
        // This is more efficient than tracking during insert
        String selectSql = """
            SELECT id, deposit_amount, outstanding_balance, status 
            FROM bookings 
            WHERE business_id = ? AND status IN ('CONFIRMED', 'COMPLETED')
            """;

        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {

            ps.setLong(1, businessId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingData data = new BookingData();
                    data.bookingId = rs.getLong("id");
                    data.depositAmount = rs.getBigDecimal("deposit_amount");
                    data.outstandingBalance = rs.getBigDecimal("outstanding_balance");
                    data.status = rs.getString("status");
                    bookingsData.add(data);
                }
            }
        }

        return bookingsData;
    }

    /**
     * Insert payments in batches for CONFIRMED/COMPLETED bookings
     */
    private int insertPaymentsBatch(Connection conn, List<BookingData> bookingsData, Long businessId) throws SQLException {
        if (bookingsData.isEmpty()) {
            return 0;
        }

        Random random = new Random();

        String sql = """
            INSERT INTO payments (booking_id, amount, stripe_payment_intent_id, status, 
                                 payment_date, created_at, business_id) 
            VALUES (?, ?, ?, 'SUCCEEDED', ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            int batchCount = 0;
            int totalInserted = 0;

            for (BookingData booking : bookingsData) {
                // Payment amount: deposit for CONFIRMED, full amount for COMPLETED
                BigDecimal amount = booking.status.equals("COMPLETED")
                    ? booking.depositAmount.add(booking.outstandingBalance)
                    : booking.depositAmount;

                LocalDateTime paymentDate = LocalDateTime.now().minusDays(random.nextInt(365));

                ps.setLong(1, booking.bookingId);
                ps.setBigDecimal(2, amount);
                ps.setString(3, "pi_seed_" + UUID.randomUUID().toString().substring(0, 24));
                ps.setTimestamp(4, Timestamp.valueOf(paymentDate));
                ps.setTimestamp(5, Timestamp.valueOf(paymentDate.minusMinutes(random.nextInt(60))));
                ps.setLong(6, businessId);
                ps.addBatch();
                batchCount++;

                if (batchCount >= batchSize) {
                    ps.executeBatch();
                    totalInserted += batchCount;
                    batchCount = 0;

                    if (totalInserted % 20000 == 0) {
                        log.info("    Inserted {}/{} payments...", totalInserted, bookingsData.size());
                    }
                }
            }

            // Execute remaining batch
            if (batchCount > 0) {
                ps.executeBatch();
                totalInserted += batchCount;
            }

            return totalInserted;
        }
    }

    /**
     * Get a weighted random booking status
     * Distribution: COMPLETED (50%), CONFIRMED (30%), CANCELLED (15%), NO_SHOW (5%)
     * No PENDING status - all bookings have paid deposits
     */
    private String getWeightedStatus(Random random) {
        int r = random.nextInt(100);
        if (r < 50) return "COMPLETED";
        if (r < 80) return "CONFIRMED";
        if (r < 95) return "CANCELLED";
        return "NO_SHOW";
    }

    /**
     * Get appropriate payment status for a given booking status
     */
    private String getPaymentStatusForBookingStatus(String bookingStatus, Random random) {
        return switch (bookingStatus) {
            case "COMPLETED" -> "FULLY_PAID";
            case "CONFIRMED" -> "DEPOSIT_PAID";
            case "CANCELLED" -> "CANCELLED";
            case "NO_SHOW" -> "DEPOSIT_PAID"; // Usually deposit was paid before no-show
            default -> "DEPOSIT_PAID"; // No more PENDING status
        };
    }

    /**
     * Calculate deposit and outstanding balance using the same logic as DepositCalculationService.
     * Balance is rounded UP to nearest €5, deposit is the remainder.
     * Minimum deposit is €0.50.
     *
     * @param totalPrice Total service price
     * @param depositPercentage Deposit percentage (e.g., 25 for 25%)
     * @return Array of [depositAmount, outstandingBalance]
     */
    private BigDecimal[] calculateDepositAndBalance(BigDecimal totalPrice, int depositPercentage) {
        BigDecimal minDeposit = BigDecimal.valueOf(0.50);

        // Calculate initial deposit amount using the percentage
        BigDecimal initialDeposit = totalPrice.multiply(BigDecimal.valueOf(depositPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Calculate target balance (remaining amount)
        BigDecimal targetBalance = totalPrice.subtract(initialDeposit);

        // Round balance UP to nearest €5
        BigDecimal roundedBalance = roundUpToNearestFive(targetBalance);

        // Calculate final deposit
        BigDecimal finalDeposit = totalPrice.subtract(roundedBalance);

        // Ensure deposit meets minimum of €0.50
        if (finalDeposit.compareTo(minDeposit) < 0) {
            finalDeposit = minDeposit;
            roundedBalance = totalPrice.subtract(minDeposit);
        }

        return new BigDecimal[]{finalDeposit, roundedBalance};
    }

    /**
     * Round amount UP to nearest €5.
     * €12.50 → €15.00
     */
    private BigDecimal roundUpToNearestFive(BigDecimal amount) {
        // Convert to cents (multiply by 100)
        long centsValue = amount.multiply(BigDecimal.valueOf(100)).longValue();

        // Round UP to nearest €5 (500 cents)
        // Using ceiling division: (value + 499) / 500
        long roundedCents = ((centsValue + 499) / 500) * 500;

        // Convert back to euros
        return BigDecimal.valueOf(roundedCents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Generate a unique phone number
     */
    private String generateUniquePhone() {
        long phone = phoneCounter.getAndIncrement();
        return "+" + phone;
    }

    /**
     * Data class to hold booking information for payment creation
     */
    private static class BookingData {
        Long bookingId;
        BigDecimal depositAmount;
        BigDecimal outstandingBalance;
        String status;
    }
}









