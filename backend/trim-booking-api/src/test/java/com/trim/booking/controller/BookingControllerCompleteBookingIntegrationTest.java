package com.trim.booking.controller;

import com.trim.booking.config.JwtUtil;
import com.trim.booking.entity.*;
import com.trim.booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BookingController.completeBooking() (PUT /api/bookings/{id}/complete)
 *
 * Tests cover:
 * - Authorization: Only BARBER and ADMIN roles can complete bookings
 * - Status transitions: PENDING -> COMPLETED, CONFIRMED -> COMPLETED
 * - Invalid transitions: Cannot complete CANCELLED or already COMPLETED bookings
 * - Payment updates: Completing a booking marks it as FULLY_PAID
 * - Booking not found scenarios
 * - Business logic: Outstanding balance set to zero on completion
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BookingControllerCompleteBookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private User testCustomer;
    private User testAdmin;
    private User barberUser;
    private Barber testBarber;
    private ServiceOffered testService;
    private String customerToken;
    private String adminToken;
    private String barberToken;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        // Clean up
        bookingRepository.deleteAll();
        serviceRepository.deleteAll();
        serviceCategoryRepository.deleteAll();
        barberRepository.deleteAll();
        userRepository.deleteAll();

        // Create test customer
        testCustomer = new User();
        testCustomer.setFirstName("Test");
        testCustomer.setLastName("Customer");
        testCustomer.setEmail("customer@test.com");
        testCustomer.setPhone("9876543210");
        testCustomer.setPasswordHash("password123");
        testCustomer.setRole(User.Role.CUSTOMER);
        testCustomer = userRepository.save(testCustomer);

        // Create admin user
        testAdmin = new User();
        testAdmin.setFirstName("Admin");
        testAdmin.setLastName("User");
        testAdmin.setEmail("admin@test.com");
        testAdmin.setPhone("1111111111");
        testAdmin.setPasswordHash("password123");
        testAdmin.setRole(User.Role.ADMIN);
        testAdmin = userRepository.save(testAdmin);

        // Create barber user
        barberUser = new User();
        barberUser.setFirstName("John");
        barberUser.setLastName("Barber");
        barberUser.setEmail("john@trim.com");
        barberUser.setPhone("1234567890");
        barberUser.setPasswordHash("password123");
        barberUser.setRole(User.Role.BARBER);
        barberUser = userRepository.save(barberUser);

        // Create test barber
        testBarber = new Barber();
        testBarber.setUser(barberUser);
        testBarber.setBio("Test barber");
        testBarber.setActive(true);
        testBarber = barberRepository.save(testBarber);

        // Create service category
        ServiceCategory category = new ServiceCategory();
        category.setName("Haircuts");
        category.setActive(true);
        category = serviceCategoryRepository.save(category);

        // Create test service
        testService = new ServiceOffered();
        testService.setName("Basic Haircut");
        testService.setDescription("Standard haircut");
        testService.setDurationMinutes(30);
        testService.setPrice(BigDecimal.valueOf(25.00));
        testService.setDepositPercentage(50);
        testService.setActive(true);
        testService.setCategory(category);
        testService = serviceRepository.save(testService);

        // Generate JWT tokens
        customerToken = jwtUtil.generateToken(testCustomer.getEmail(), testCustomer.getRole().name(), testCustomer.getId());
        adminToken = jwtUtil.generateToken(testAdmin.getEmail(), testAdmin.getRole().name(), testAdmin.getId());
        barberToken = jwtUtil.generateToken(barberUser.getEmail(), barberUser.getRole().name(), barberUser.getId());

        // Use a future Monday for testing
        futureDate = LocalDate.now().plusWeeks(1);
        while (futureDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            futureDate = futureDate.plusDays(1);
        }
    }

    // Helper method to create bookings with different statuses
    private Booking createBooking(Booking.BookingStatus status, Booking.PaymentStatus paymentStatus) {
        Booking booking = new Booking();
        booking.setCustomer(testCustomer);
        booking.setBarber(testBarber);
        booking.setService(testService);
        booking.setBookingDate(futureDate);
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(10, 30));
        booking.setStatus(status);
        booking.setPaymentStatus(paymentStatus);
        booking.setDepositAmount(BigDecimal.valueOf(12.50));
        booking.setOutstandingBalance(BigDecimal.valueOf(12.50));
        return bookingRepository.save(booking);
    }

    // ==================== Authorization Tests ====================

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("BARBER should be able to complete a booking")
        void testCompleteBooking_BarberAuthorized() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                    .andExpect(jsonPath("$.status", is("COMPLETED")));
        }

        @Test
        @DisplayName("ADMIN should be able to complete a booking")
        void testCompleteBooking_AdminAuthorized() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                    .andExpect(jsonPath("$.status", is("COMPLETED")));
        }

        @Test
        @DisplayName("CUSTOMER should NOT be able to complete a booking")
        void testCompleteBooking_CustomerForbidden() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + customerToken))
                    .andExpect(status().isForbidden());

            // Verify booking status unchanged
            Booking unchanged = bookingRepository.findById(booking.getId()).orElseThrow();
            assertEquals(Booking.BookingStatus.CONFIRMED, unchanged.getStatus());
        }

        @Test
        @DisplayName("Unauthenticated request should be rejected")
        void testCompleteBooking_Unauthenticated() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId()))
                    .andExpect(status().isForbidden());

            // Verify booking status unchanged
            Booking unchanged = bookingRepository.findById(booking.getId()).orElseThrow();
            assertEquals(Booking.BookingStatus.CONFIRMED, unchanged.getStatus());
        }
    }

    // ==================== Status Transition Tests ====================

    @Nested
    @DisplayName("Status Transition Tests")
    class StatusTransitionTests {

        @Test
        @DisplayName("Should complete a PENDING booking")
        void testCompleteBooking_FromPending() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.PENDING, Booking.PaymentStatus.PENDING);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("COMPLETED")));

            // Verify in database
            Booking completed = bookingRepository.findById(booking.getId()).orElseThrow();
            assertEquals(Booking.BookingStatus.COMPLETED, completed.getStatus());
        }

        @Test
        @DisplayName("Should complete a CONFIRMED booking")
        void testCompleteBooking_FromConfirmed() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("COMPLETED")));

            // Verify in database
            Booking completed = bookingRepository.findById(booking.getId()).orElseThrow();
            assertEquals(Booking.BookingStatus.COMPLETED, completed.getStatus());
        }

        @Test
        @DisplayName("Should complete a NO_SHOW booking")
        void testCompleteBooking_FromNoShow() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.NO_SHOW, Booking.PaymentStatus.DEPOSIT_PAID);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("COMPLETED")));
        }

        @Test
        @DisplayName("Should reject completing a CANCELLED booking")
        void testCompleteBooking_FromCancelled() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CANCELLED, Booking.PaymentStatus.CANCELLED);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isBadRequest());

            // Verify status unchanged
            Booking unchanged = bookingRepository.findById(booking.getId()).orElseThrow();
            assertEquals(Booking.BookingStatus.CANCELLED, unchanged.getStatus());
        }

        @Test
        @DisplayName("Should reject completing an already COMPLETED booking")
        void testCompleteBooking_AlreadyCompleted() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.COMPLETED, Booking.PaymentStatus.FULLY_PAID);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== Payment Status Tests ====================

    @Nested
    @DisplayName("Payment Status Tests")
    class PaymentStatusTests {

        @Test
        @DisplayName("Completing booking should set payment status to FULLY_PAID")
        void testCompleteBooking_SetsFullyPaid() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentStatus", is("FULLY_PAID")));

            // Verify in database
            Booking completed = bookingRepository.findById(booking.getId()).orElseThrow();
            assertEquals(Booking.PaymentStatus.FULLY_PAID, completed.getPaymentStatus());
        }

        @Test
        @DisplayName("Completing booking should set outstanding balance to zero")
        void testCompleteBooking_SetsOutstandingBalanceToZero() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);
            // Verify there is an outstanding balance initially
            assertEquals(BigDecimal.valueOf(12.50).setScale(2), booking.getOutstandingBalance().setScale(2));

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.outstandingBalance", is(0)));

            // Verify in database
            Booking completed = bookingRepository.findById(booking.getId()).orElseThrow();
            assertEquals(0, BigDecimal.ZERO.compareTo(completed.getOutstandingBalance()));
        }

        @Test
        @DisplayName("Completing booking should set deposit amount to full service price")
        void testCompleteBooking_SetsDepositAmountToFullPrice() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.depositAmount", is(25.0)));

            // Verify in database
            Booking completed = bookingRepository.findById(booking.getId()).orElseThrow();
            assertEquals(testService.getPrice().setScale(2), completed.getDepositAmount().setScale(2));
        }

        @Test
        @DisplayName("Completing a PENDING payment booking should mark as FULLY_PAID")
        void testCompleteBooking_PendingPayment() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.PENDING, Booking.PaymentStatus.PENDING);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("COMPLETED")))
                    .andExpect(jsonPath("$.paymentStatus", is("FULLY_PAID")));
        }
    }

    // ==================== Not Found Tests ====================

    @Nested
    @DisplayName("Not Found Tests")
    class NotFoundTests {

        @Test
        @DisplayName("Should return 404 when booking not found")
        void testCompleteBooking_NotFound() throws Exception {
            mockMvc.perform(put("/api/bookings/{id}/complete", 99999L)
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when booking ID is 0")
        void testCompleteBooking_ZeroId() throws Exception {
            mockMvc.perform(put("/api/bookings/{id}/complete", 0L)
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== Business Logic Tests ====================

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Barber can complete booking assigned to them")
        void testCompleteBooking_BarberCompletesOwnBooking() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);
            // Booking is assigned to testBarber, and barberToken is for barberUser who owns testBarber

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("COMPLETED")));
        }

        @Test
        @DisplayName("Admin can complete any booking regardless of assignment")
        void testCompleteBooking_AdminCompletesAnyBooking() throws Exception {
            // Create a second barber
            User secondBarberUser = new User();
            secondBarberUser.setFirstName("Jane");
            secondBarberUser.setLastName("Barber");
            secondBarberUser.setEmail("jane@trim.com");
            secondBarberUser.setPhone("2222222222");
            secondBarberUser.setPasswordHash("password123");
            secondBarberUser.setRole(User.Role.BARBER);
            secondBarberUser = userRepository.save(secondBarberUser);

            Barber secondBarber = new Barber();
            secondBarber.setUser(secondBarberUser);
            secondBarber.setBio("Second barber");
            secondBarber.setActive(true);
            secondBarber = barberRepository.save(secondBarber);

            // Create booking assigned to second barber
            Booking booking = new Booking();
            booking.setCustomer(testCustomer);
            booking.setBarber(secondBarber);
            booking.setService(testService);
            booking.setBookingDate(futureDate);
            booking.setStartTime(LocalTime.of(11, 0));
            booking.setEndTime(LocalTime.of(11, 30));
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
            booking.setDepositAmount(BigDecimal.valueOf(12.50));
            booking.setOutstandingBalance(BigDecimal.valueOf(12.50));
            booking = bookingRepository.save(booking);

            // Admin should be able to complete booking assigned to any barber
            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("COMPLETED")));
        }

        @Test
        @DisplayName("Completing booking returns full booking details in response")
        void testCompleteBooking_ReturnsFullDetails() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                    .andExpect(jsonPath("$.status", is("COMPLETED")))
                    .andExpect(jsonPath("$.paymentStatus", is("FULLY_PAID")))
                    .andExpect(jsonPath("$.customer").exists())
                    .andExpect(jsonPath("$.barber").exists())
                    .andExpect(jsonPath("$.service").exists())
                    .andExpect(jsonPath("$.bookingDate", is(futureDate.toString())))
                    .andExpect(jsonPath("$.startTime", is("10:00:00")))
                    .andExpect(jsonPath("$.endTime", is("10:30:00")));
        }

        @Test
        @DisplayName("Completing booking is persisted in database")
        void testCompleteBooking_PersistsToDatabase() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);
            Long bookingId = booking.getId();

            mockMvc.perform(put("/api/bookings/{id}/complete", bookingId)
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk());

            // Fetch from database and verify all fields
            Booking persisted = bookingRepository.findById(bookingId).orElseThrow();
            assertEquals(Booking.BookingStatus.COMPLETED, persisted.getStatus());
            assertEquals(Booking.PaymentStatus.FULLY_PAID, persisted.getPaymentStatus());
            assertEquals(0, BigDecimal.ZERO.compareTo(persisted.getOutstandingBalance()));
            assertEquals(testService.getPrice().setScale(2), persisted.getDepositAmount().setScale(2));
        }
    }

    // ==================== Edge Cases ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Complete booking with different service prices")
        void testCompleteBooking_DifferentServicePrices() throws Exception {
            // Create expensive service
            ServiceOffered expensiveService = new ServiceOffered();
            expensiveService.setName("Premium Cut");
            expensiveService.setDescription("Premium service");
            expensiveService.setDurationMinutes(60);
            expensiveService.setPrice(BigDecimal.valueOf(75.00));
            expensiveService.setDepositPercentage(50);
            expensiveService.setActive(true);
            expensiveService.setCategory(testService.getCategory());
            expensiveService = serviceRepository.save(expensiveService);

            // Create booking with expensive service
            Booking booking = new Booking();
            booking.setCustomer(testCustomer);
            booking.setBarber(testBarber);
            booking.setService(expensiveService);
            booking.setBookingDate(futureDate);
            booking.setStartTime(LocalTime.of(14, 0));
            booking.setEndTime(LocalTime.of(15, 0));
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
            booking.setDepositAmount(BigDecimal.valueOf(37.50));
            booking.setOutstandingBalance(BigDecimal.valueOf(37.50));
            booking = bookingRepository.save(booking);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.depositAmount", is(75.0)))
                    .andExpect(jsonPath("$.outstandingBalance", is(0)));
        }

        @Test
        @DisplayName("Complete multiple bookings sequentially")
        void testCompleteBooking_MultipleBookings() throws Exception {
            Booking booking1 = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

            Booking booking2 = new Booking();
            booking2.setCustomer(testCustomer);
            booking2.setBarber(testBarber);
            booking2.setService(testService);
            booking2.setBookingDate(futureDate);
            booking2.setStartTime(LocalTime.of(11, 0));
            booking2.setEndTime(LocalTime.of(11, 30));
            booking2.setStatus(Booking.BookingStatus.CONFIRMED);
            booking2.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
            booking2.setDepositAmount(BigDecimal.valueOf(12.50));
            booking2.setOutstandingBalance(BigDecimal.valueOf(12.50));
            booking2 = bookingRepository.save(booking2);

            // Complete first booking
            mockMvc.perform(put("/api/bookings/{id}/complete", booking1.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("COMPLETED")));

            // Complete second booking
            mockMvc.perform(put("/api/bookings/{id}/complete", booking2.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("COMPLETED")));

            // Verify both are completed
            assertEquals(Booking.BookingStatus.COMPLETED,
                    bookingRepository.findById(booking1.getId()).orElseThrow().getStatus());
            assertEquals(Booking.BookingStatus.COMPLETED,
                    bookingRepository.findById(booking2.getId()).orElseThrow().getStatus());
        }

        @Test
        @DisplayName("Complete booking with zero deposit amount initially")
        void testCompleteBooking_ZeroInitialDeposit() throws Exception {
            Booking booking = new Booking();
            booking.setCustomer(testCustomer);
            booking.setBarber(testBarber);
            booking.setService(testService);
            booking.setBookingDate(futureDate);
            booking.setStartTime(LocalTime.of(15, 0));
            booking.setEndTime(LocalTime.of(15, 30));
            booking.setStatus(Booking.BookingStatus.PENDING);
            booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
            booking.setDepositAmount(BigDecimal.ZERO);
            booking.setOutstandingBalance(testService.getPrice());
            booking = bookingRepository.save(booking);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("COMPLETED")))
                    .andExpect(jsonPath("$.paymentStatus", is("FULLY_PAID")))
                    .andExpect(jsonPath("$.depositAmount", is(25.0)))
                    .andExpect(jsonPath("$.outstandingBalance", is(0)));
        }

        @Test
        @DisplayName("Complete booking preserves other booking fields")
        void testCompleteBooking_PreservesOtherFields() throws Exception {
            Booking booking = new Booking();
            booking.setCustomer(testCustomer);
            booking.setBarber(testBarber);
            booking.setService(testService);
            booking.setBookingDate(futureDate);
            booking.setStartTime(LocalTime.of(16, 0));
            booking.setEndTime(LocalTime.of(16, 30));
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
            booking.setDepositAmount(BigDecimal.valueOf(12.50));
            booking.setOutstandingBalance(BigDecimal.valueOf(12.50));
            booking.setNotes("Special request: extra styling");
            booking = bookingRepository.save(booking);

            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk());

            // Verify other fields are preserved
            Booking completed = bookingRepository.findById(booking.getId()).orElseThrow();
            assertEquals("Special request: extra styling", completed.getNotes());
            assertEquals(testCustomer.getId(), completed.getCustomer().getId());
            assertEquals(testBarber.getId(), completed.getBarber().getId());
            assertEquals(testService.getId(), completed.getService().getId());
            assertEquals(LocalTime.of(16, 0), completed.getStartTime());
            assertEquals(LocalTime.of(16, 30), completed.getEndTime());
        }
    }

    // ==================== Barber-specific Authorization Tests ====================

    @Nested
    @DisplayName("Barber-specific Tests")
    class BarberSpecificTests {

        @Test
        @DisplayName("Barber can complete booking assigned to another barber")
        void testCompleteBooking_BarberCompletesOtherBarbersBooking() throws Exception {
            // Create a second barber
            User secondBarberUser = new User();
            secondBarberUser.setFirstName("Jane");
            secondBarberUser.setLastName("Barber");
            secondBarberUser.setEmail("jane2@trim.com");
            secondBarberUser.setPhone("3333333333");
            secondBarberUser.setPasswordHash("password123");
            secondBarberUser.setRole(User.Role.BARBER);
            secondBarberUser = userRepository.save(secondBarberUser);

            Barber secondBarber = new Barber();
            secondBarber.setUser(secondBarberUser);
            secondBarber.setBio("Second barber");
            secondBarber.setActive(true);
            secondBarber = barberRepository.save(secondBarber);

            // Create booking assigned to second barber
            Booking booking = new Booking();
            booking.setCustomer(testCustomer);
            booking.setBarber(secondBarber);
            booking.setService(testService);
            booking.setBookingDate(futureDate);
            booking.setStartTime(LocalTime.of(12, 0));
            booking.setEndTime(LocalTime.of(12, 30));
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
            booking.setDepositAmount(BigDecimal.valueOf(12.50));
            booking.setOutstandingBalance(BigDecimal.valueOf(12.50));
            booking = bookingRepository.save(booking);

            // First barber tries to complete second barber's booking
            // Based on current implementation, any barber can complete any booking
            mockMvc.perform(put("/api/bookings/{id}/complete", booking.getId())
                            .header("Authorization", "Bearer " + barberToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("COMPLETED")));
        }
    }
}

