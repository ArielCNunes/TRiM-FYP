package gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Gatling load test simulation for the Trim Booking API.
 *
 * This simulation tests the main API endpoints with database operations:
 * - Public endpoints: barbers, services, availability
 * - Authenticated endpoints: bookings, dashboard
 *
 */
public class BookingApiSimulation extends Simulation {

    // ============================================
    // CONFIGURATION - Update these for your setup
    // ============================================

    private static final String BASE_URL = "http://localhost:8080";
    private static final String BUSINESS_SLUG = "business-1"; // Seeder creates business-1, business-2, etc.

    // Test user credentials (created by DataSeeder with password123)
    private static final String CUSTOMER_EMAIL = "customer1@business-1.test";
    private static final String CUSTOMER_PASSWORD = "password123";

    private static final String ADMIN_EMAIL = "admin@business-1.test";
    private static final String ADMIN_PASSWORD = "password123";

    // Test data IDs (must exist in seeded database)
    private static final int BARBER_ID = 1;
    private static final int SERVICE_ID = 1;

    // ============================================
    // HTTP CONFIGURATION
    // ============================================

    private HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .header("X-Business-Slug", BUSINESS_SLUG);

    // ============================================
    // FEEDERS - Data generators
    // ============================================

    // Generate random future dates for availability checks
    private Iterator<Map<String, Object>> dateFeeder = Stream.generate((Supplier<Map<String, Object>>) () -> {
        LocalDate futureDate = LocalDate.now().plusDays(new Random().nextInt(30) + 1);
        return Map.of("bookingDate", futureDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }).iterator();

    // Generate random time slots
    private Iterator<Map<String, Object>> timeFeeder = Stream.generate((Supplier<Map<String, Object>>) () -> {
        String[] times = {"09:00", "10:00", "11:00", "14:00", "15:00", "16:00"};
        return Map.of("startTime", times[new Random().nextInt(times.length)]);
    }).iterator();

    // ============================================
    // SCENARIO 1: Browse Services (Public - No Auth)
    // ============================================

    private ScenarioBuilder browseServicesScenario = scenario("Browse Services (Public)")
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
            .feed(dateFeeder)
            .exec(
                http("Check Availability")
                    .get("/api/availability")
                    .queryParam("barberId", BARBER_ID)
                    .queryParam("date", "#{bookingDate}")
                    .queryParam("serviceId", SERVICE_ID)
                    .check(status().is(200))
            );

    // ============================================
    // SCENARIO 2: Customer Booking Flow (Authenticated)
    // ============================================

    private ScenarioBuilder customerBookingScenario = scenario("Customer Booking Flow")
            // Login first
            .exec(
                http("Customer Login")
                    .post("/api/auth/login")
                    .body(StringBody(String.format(
                        "{\"email\":\"%s\",\"password\":\"%s\"}",
                        CUSTOMER_EMAIL, CUSTOMER_PASSWORD
                    )))
                    .check(status().saveAs("loginStatus"))
                    .check(bodyString().saveAs("loginResponse"))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("authToken"))
                    .check(jsonPath("$.id").saveAs("customerId"))
            )
            .exec(session -> {
                if (!"200".equals(session.getString("loginStatus"))) {
                    System.out.println("Customer Login FAILED! Status: " + session.getString("loginStatus"));
                    System.out.println("Response: " + session.getString("loginResponse"));
                }
                return session;
            })
            .pause(1)
            // Browse available services
            .exec(
                http("Get Services")
                    .get("/api/services/active")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // Get barbers
            .exec(
                http("Get Barbers")
                    .get("/api/barbers/active")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // Check availability
            .feed(dateFeeder)
            .exec(
                http("Check Availability")
                    .get("/api/availability")
                    .queryParam("barberId", BARBER_ID)
                    .queryParam("date", "#{bookingDate}")
                    .queryParam("serviceId", SERVICE_ID)
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // View my bookings
            .exec(
                http("Get My Bookings")
                    .get("/api/bookings/customer/#{customerId}")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            );

    // ============================================
    // SCENARIO 3: Admin Dashboard (Authenticated)
    // ============================================

    private ScenarioBuilder adminDashboardScenario = scenario("Admin Dashboard")
            // Login as admin
            .exec(
                http("Admin Login")
                    .post("/api/auth/login")
                    .body(StringBody(String.format(
                        "{\"email\":\"%s\",\"password\":\"%s\"}",
                        ADMIN_EMAIL, ADMIN_PASSWORD
                    )))
                    .check(status().saveAs("adminLoginStatus"))
                    .check(bodyString().saveAs("adminLoginResponse"))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("authToken"))
            )
            .exec(session -> {
                if (!"200".equals(session.getString("adminLoginStatus"))) {
                    System.out.println("Admin Login FAILED! Status: " + session.getString("adminLoginStatus"));
                    System.out.println("Response: " + session.getString("adminLoginResponse"));
                }
                return session;
            })
            .pause(1)
            // Get dashboard stats
            .exec(
                http("Get Dashboard Stats")
                    .get("/api/dashboard/admin")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // Get all bookings
            .exec(
                http("Get All Bookings")
                    .get("/api/bookings/all")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // Get barbers
            .exec(
                http("Get All Barbers")
                    .get("/api/barbers")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            )
            .pause(1)
            // Get all services
            .exec(
                http("Get All Services")
                    .get("/api/services")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            );

    // ============================================
    // SCENARIO 4: Heavy Availability Checks (Stress Test)
    // ============================================

    private ScenarioBuilder availabilityStressScenario = scenario("Availability Stress Test")
            .repeat(5).on(
                feed(dateFeeder)
                .exec(
                    http("Check Availability (Stress)")
                        .get("/api/availability")
                        .queryParam("barberId", BARBER_ID)
                        .queryParam("date", "#{bookingDate}")
                        .queryParam("serviceId", SERVICE_ID)
                        .check(status().is(200))
                )
                .pause(java.time.Duration.ofMillis(500))  // 500ms between requests
            );

    // ============================================
    // LOAD PROFILES
    // ============================================

    {
        setUp(
            // Scenario 1: Public browsing - highest load
            browseServicesScenario.injectOpen(
                rampUsers(20).during(30),     // Ramp up to 20 users over 30 seconds
                constantUsersPerSec(5).during(60)  // Then 5 users/sec for 60 seconds
            ),

            // Scenario 2: Customer booking flow - moderate load
            customerBookingScenario.injectOpen(
                rampUsers(10).during(30),     // Ramp up to 10 users over 30 seconds
                constantUsersPerSec(2).during(60)  // Then 2 users/sec for 60 seconds
            ),

            // Scenario 3: Admin dashboard - light load
            adminDashboardScenario.injectOpen(
                rampUsers(5).during(30),      // Ramp up to 5 users over 30 seconds
                constantUsersPerSec(1).during(60)  // Then 1 user/sec for 60 seconds
            ),

            // Scenario 4: Availability stress test
            availabilityStressScenario.injectOpen(
                rampUsers(15).during(30)      // 15 users hammering availability
            )
        )
        .protocols(httpProtocol)
        .assertions(
            // Baseline assertions
            global().responseTime().percentile3().lt(10000),  // 95th percentile < 10 seconds (baseline)
            global().responseTime().percentile4().lt(20000),  // 99th percentile < 20 seconds (baseline)
            global().successfulRequests().percent().gt(95.0)  // >95% success rate
        );
    }
}
