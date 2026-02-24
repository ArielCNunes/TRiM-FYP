package gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Gatling load test simulation for the Trim Booking API.
 *
 * Tests ALL app functionality with both reads and writes:
 *
 * Scenario 1: Public Browsing (read)         - barbers, services, availability, categories
 * Scenario 2: Customer Registration (write)   - register new customer accounts
 * Scenario 3: Customer Booking Flow (r+w)     - login, browse, create booking, view, cancel
 * Scenario 4: Admin Management (r+w)          - login, dashboard, manage bookings, services
 * Scenario 5: Barber Operations (r+w)         - login, view schedule, complete/no-show bookings
 * Scenario 6: Availability Stress Test (read) - rapid-fire availability checks
 *
 * Total: 5000 concurrent users
 */
public class BookingApiSimulation extends Simulation {

    // ============================================
    // CONFIGURATION
    // ============================================

    private static final String BASE_URL = "http://localhost:8080";
    private static final String BUSINESS_SLUG = "business-1";

    // Seeded credentials (DataSeeder uses password123 for all)
    private static final String CUSTOMER_EMAIL = "customer1@business-1.test";
    private static final String CUSTOMER_PASSWORD = "password123";

    private static final String ADMIN_EMAIL = "admin@business-1.test";
    private static final String ADMIN_PASSWORD = "password123";

    private static final String BARBER_EMAIL = "barber1@business-1.test";
    private static final String BARBER_PASSWORD = "password123";

    // Seeded IDs (from DataSeeder)
    private static final int BARBER_ID = 1;
    private static final int SERVICE_ID = 1;

    // Unique counters for registration (avoid duplicate emails/phones)
    private static final AtomicLong emailCounter = new AtomicLong(System.currentTimeMillis());
    private static final AtomicLong phoneCounter = new AtomicLong(3538500000L);

    // ============================================
    // HTTP CONFIGURATION
    // ============================================

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .header("X-Business-Slug", BUSINESS_SLUG);

    // ============================================
    // FEEDERS
    // ============================================

    // Random future dates (1-30 days ahead, skip Sundays)
    private final Iterator<Map<String, Object>> dateFeeder = Stream.generate((Supplier<Map<String, Object>>) () -> {
        Random rnd = new Random();
        LocalDate date;
        do {
            date = LocalDate.now().plusDays(rnd.nextInt(30) + 1);
        } while (date.getDayOfWeek().getValue() == 7); // skip Sundays
        return Map.of("bookingDate", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }).iterator();

    // Random time slots within working hours
    private final Iterator<Map<String, Object>> timeFeeder = Stream.generate((Supplier<Map<String, Object>>) () -> {
        String[] times = {"09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00"};
        return Map.of("startTime", times[new Random().nextInt(times.length)]);
    }).iterator();

    // Unique registration data per virtual user
    private final Iterator<Map<String, Object>> registrationFeeder = Stream.generate((Supplier<Map<String, Object>>) () -> {
        long id = emailCounter.incrementAndGet();
        long phone = phoneCounter.incrementAndGet();
        return Map.of(
            "regEmail", "loadtest_" + id + "@test.com",
            "regPhone", "+" + phone,
            "regFirst", "Load",
            "regLast", "User" + id
        );
    }).iterator();

    // Random barber IDs (1-10 from seeder)
    private final Iterator<Map<String, Object>> barberIdFeeder = Stream.generate((Supplier<Map<String, Object>>) () ->
        Map.<String, Object>of("randomBarberId", new Random().nextInt(10) + 1)
    ).iterator();

    // Random service IDs (1-20 from seeder: 5 categories × 4 services)
    private final Iterator<Map<String, Object>> serviceIdFeeder = Stream.generate((Supplier<Map<String, Object>>) () ->
        Map.<String, Object>of("randomServiceId", new Random().nextInt(20) + 1)
    ).iterator();

    // ============================================
    // SCENARIO 1: Public Browsing (READ only)
    // Browse services, barbers, availability, categories
    // ============================================

    private final ScenarioBuilder publicBrowsingScenario = scenario("1 - Public Browsing")
            .exec(
                http("Get Active Barbers")
                    .get("/api/barbers/active")
                    .check(status().is(200))
                    .check(jsonPath("$[*]").exists())
            )
            .pause(1)
            .exec(
                http("Get Active Services")
                    .get("/api/services/active")
                    .check(status().is(200))
                    .check(jsonPath("$[*]").exists())
            )
            .pause(1)
            .exec(
                http("Get Categories With Services")
                    .get("/api/categories/with-services")
                    .check(status().is(200))
            )
            .pause(1)
            .feed(dateFeeder)
            .feed(barberIdFeeder)
            .feed(serviceIdFeeder)
            .exec(
                http("Check Availability")
                    .get("/api/availability")
                    .queryParam("barberId", "#{randomBarberId}")
                    .queryParam("date", "#{bookingDate}")
                    .queryParam("serviceId", "#{randomServiceId}")
                    .check(status().is(200))
            )
;

    // ============================================
    // SCENARIO 2: Customer Registration (WRITE)
    // Register new customer accounts
    // ============================================

    private final ScenarioBuilder registrationScenario = scenario("2 - Customer Registration")
            .feed(registrationFeeder)
            .exec(
                http("Register Customer")
                    .post("/api/auth/register")
                    .body(StringBody(
                        "{\"firstName\":\"#{regFirst}\",\"lastName\":\"#{regLast}\"," +
                        "\"email\":\"#{regEmail}\",\"password\":\"password123\"," +
                        "\"phone\":\"#{regPhone}\"}"
                    ))
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("newUserId"))
            )
            .pause(1)
            // Login with the newly registered account
            .exec(
                http("Login New Customer")
                    .post("/api/auth/login")
                    .body(StringBody(
                        "{\"email\":\"#{regEmail}\",\"password\":\"password123\"}"
                    ))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("authToken"))
            )
            .pause(1)
            // Browse as new customer
            .exec(
                http("New Customer - Get Services")
                    .get("/api/services/active")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            );

    // ============================================
    // SCENARIO 3: Customer Booking Flow (READ + WRITE)
    // Login → browse → create booking → view bookings → cancel booking
    // ============================================

    private final ScenarioBuilder customerBookingScenario = scenario("3 - Customer Booking Flow")
            // Login
            .exec(
                http("Customer Login")
                    .post("/api/auth/login")
                    .body(StringBody(String.format(
                        "{\"email\":\"%s\",\"password\":\"%s\"}",
                        CUSTOMER_EMAIL, CUSTOMER_PASSWORD
                    )))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("authToken"))
                    .check(jsonPath("$.id").saveAs("customerId"))
            )
            .pause(1)
            // Browse services
            .exec(
                http("Get Services")
                    .get("/api/services/active")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // Browse barbers
            .exec(
                http("Get Barbers")
                    .get("/api/barbers/active")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // Check availability
            .feed(dateFeeder)
            .feed(timeFeeder)
            .feed(barberIdFeeder)
            .feed(serviceIdFeeder)
            .exec(
                http("Check Availability")
                    .get("/api/availability")
                    .queryParam("barberId", "#{randomBarberId}")
                    .queryParam("date", "#{bookingDate}")
                    .queryParam("serviceId", "#{randomServiceId}")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // CREATE BOOKING (WRITE)
            .exec(
                http("Create Booking")
                    .post("/api/bookings")
                    .header("Authorization", "Bearer #{authToken}")
                    .body(StringBody(
                        "{\"barberId\":#{randomBarberId},\"serviceId\":#{randomServiceId}," +
                        "\"bookingDate\":\"#{bookingDate}\",\"startTime\":\"#{startTime}\"," +
                        "\"paymentMethod\":\"pay_in_shop\"}"
                    ))
                    .check(status().saveAs("createBookingStatus"))
                    .check(bodyString().saveAs("createBookingResponse"))
                    // Save booking ID if created successfully (201)
                    .check(jsonPath("$.id").optional().saveAs("newBookingId"))
            )
            .pause(1)
            // View my bookings (READ)
            .exec(
                http("Get My Bookings")
                    .get("/api/bookings/customer/#{customerId}")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // CANCEL BOOKING (WRITE) - only if booking was created
            .doIf(session -> "201".equals(session.getString("createBookingStatus"))).then(
                exec(
                    http("Cancel Booking")
                        .patch("/api/bookings/#{newBookingId}/cancel")
                        .header("Authorization", "Bearer #{authToken}")
                        .check(status().in(200, 400)) // 400 if already cancelled
                )
            );

    // ============================================
    // SCENARIO 4: Admin Management (READ + WRITE)
    // Login → dashboard → view/manage bookings → manage services
    // ============================================

    private final ScenarioBuilder adminManagementScenario = scenario("4 - Admin Management")
            // Login as admin
            .exec(
                http("Admin Login")
                    .post("/api/auth/login")
                    .body(StringBody(String.format(
                        "{\"email\":\"%s\",\"password\":\"%s\"}",
                        ADMIN_EMAIL, ADMIN_PASSWORD
                    )))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("authToken"))
            )
            .pause(1)
            // Dashboard stats (READ)
            .exec(
                http("Get Dashboard Stats")
                    .get("/api/dashboard/admin")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // Get all bookings (READ)
            .exec(
                http("Get All Bookings")
                    .get("/api/bookings/all")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
                    // Save a booking ID for status updates
                    .check(jsonPath("$[0].id").optional().saveAs("adminBookingId"))
            )
            .pause(1)
            // Get all barbers (READ)
            .exec(
                http("Get All Barbers")
                    .get("/api/barbers")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // Get all services (READ)
            .exec(
                http("Get All Services")
                    .get("/api/services")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // Get categories (READ)
            .exec(
                http("Get Categories")
                    .get("/api/categories")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // Get barber availability (READ)
            .exec(
                http("Get Barber Availability")
                    .get("/api/barber-availability")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // MARK BOOKING PAID (WRITE) - if booking exists
            .doIf(session -> session.getString("adminBookingId") != null).then(
                exec(
                    http("Mark Booking Paid")
                        .put("/api/bookings/#{adminBookingId}/mark-paid")
                        .header("Authorization", "Bearer #{authToken}")
                        .check(status().in(200, 400, 403)) // 400 if wrong state, 403 if permissions
                )
            );

    // ============================================
    // SCENARIO 5: Barber Operations (READ + WRITE)
    // Login → view schedule → complete bookings → no-show bookings
    // ============================================

    private final ScenarioBuilder barberOperationsScenario = scenario("5 - Barber Operations")
            // Login as barber
            .exec(
                http("Barber Login")
                    .post("/api/auth/login")
                    .body(StringBody(String.format(
                        "{\"email\":\"%s\",\"password\":\"%s\"}",
                        BARBER_EMAIL, BARBER_PASSWORD
                    )))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("authToken"))
                    .check(jsonPath("$.barberId").optional().saveAs("barberId"))
            )
            .pause(1)
            // View today's schedule (READ)
            .exec(
                http("Get Barber Schedule")
                    .get("/api/bookings/barber/" + BARBER_ID + "/schedule")
                    .queryParam("date", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().in(200, 403))
                    // Save a booking ID from schedule
                    .check(jsonPath("$[0].id").optional().saveAs("barberBookingId"))
            )
            .pause(1)
            // View barber's breaks (READ)
            .exec(
                http("Get Barber Breaks")
                    .get("/api/barber-breaks/barber/" + BARBER_ID)
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().in(200, 403))
            )
            .pause(1)
            // View barber availability (READ)
            .exec(
                http("Get Barber Availability")
                    .get("/api/barber-availability/barber/" + BARBER_ID)
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // COMPLETE BOOKING (WRITE) - if booking exists
            .doIf(session -> session.getString("barberBookingId") != null).then(
                exec(
                    http("Complete Booking")
                        .put("/api/bookings/#{barberBookingId}/complete")
                        .header("Authorization", "Bearer #{authToken}")
                        .check(status().in(200, 400, 403)) // 400 if already completed
                )
            )
            .pause(1)
            // Check availability as barber (READ)
            .feed(dateFeeder)
            .exec(
                http("Barber Check Availability")
                    .get("/api/availability")
                    .queryParam("barberId", BARBER_ID)
                    .queryParam("date", "#{bookingDate}")
                    .queryParam("serviceId", SERVICE_ID)
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            );

    // ============================================
    // SCENARIO 6: Availability Stress Test (READ)
    // Rapid-fire availability checks across all barbers
    // ============================================

    private final ScenarioBuilder availabilityStressScenario = scenario("6 - Availability Stress Test")
            .repeat(5).on(
                feed(dateFeeder)
                .feed(barberIdFeeder)
                .feed(serviceIdFeeder)
                .exec(
                    http("Check Availability (Stress)")
                        .get("/api/availability")
                        .queryParam("barberId", "#{randomBarberId}")
                        .queryParam("date", "#{bookingDate}")
                        .queryParam("serviceId", "#{randomServiceId}")
                        .check(status().is(200))
                )
                .pause(java.time.Duration.ofMillis(500))
            );

    // ============================================
    // LOAD PROFILES - 5000 concurrent users total
    // ============================================

    {
        setUp(
            // Scenario 1: Public browsing - highest load (35% = 1750 users)
            publicBrowsingScenario.injectClosed(
                rampConcurrentUsers(0).to(1750).during(60),
                constantConcurrentUsers(1750).during(90)
            ),

            // Scenario 2: Customer registration (10% = 500 users) - WRITE heavy
            registrationScenario.injectClosed(
                rampConcurrentUsers(0).to(500).during(60),
                constantConcurrentUsers(500).during(90)
            ),

            // Scenario 3: Customer booking flow (25% = 1250 users) - READ + WRITE
            customerBookingScenario.injectClosed(
                rampConcurrentUsers(0).to(1250).during(60),
                constantConcurrentUsers(1250).during(90)
            ),

            // Scenario 4: Admin management (10% = 500 users) - READ + WRITE
            adminManagementScenario.injectClosed(
                rampConcurrentUsers(0).to(500).during(60),
                constantConcurrentUsers(500).during(90)
            ),

            // Scenario 5: Barber operations (10% = 500 users) - READ + WRITE
            barberOperationsScenario.injectClosed(
                rampConcurrentUsers(0).to(500).during(60),
                constantConcurrentUsers(500).during(90)
            ),

            // Scenario 6: Availability stress test (10% = 500 users) - READ
            availabilityStressScenario.injectClosed(
                rampConcurrentUsers(0).to(500).during(60),
                constantConcurrentUsers(500).during(90)
            )
        )
        .protocols(httpProtocol)
        .assertions(
            global().responseTime().percentile3().lt(15000),  // 95th percentile < 15s
            global().responseTime().percentile4().lt(20000),  // 99th percentile < 20s
            global().successfulRequests().percent().gt(90.0)  // >90% success rate (writes may conflict)
        );
    }
}
