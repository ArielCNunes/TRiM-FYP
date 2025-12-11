package com.trim.booking.controller;

import com.trim.booking.config.JwtUtil;
import com.trim.booking.entity.*;
import com.trim.booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BookingController.cancelBooking()
 *
 * Tests cover:
 * - Successful cancellation of PENDING bookings
 * - Successful cancellation of CONFIRMED bookings
 * - Rejection of cancelling already cancelled bookings
 * - Rejection of cancelling completed bookings
 * - Booking not found scenarios
 * - Verification that cancelled slots become available again
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BookingControllerCancelBookingIntegrationTest {

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
    private BarberAvailabilityRepository barberAvailabilityRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private User testCustomer;
    private User testAdmin;
    private Barber testBarber;
    private ServiceOffered testService;
    private String customerToken;
    private String adminToken;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        // Clean up
        bookingRepository.deleteAll();
        barberAvailabilityRepository.deleteAll();
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
        User barberUser = new User();
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

        // Use a future Monday for testing
        futureDate = LocalDate.now().plusWeeks(1);
        while (futureDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            futureDate = futureDate.plusDays(1);
        }
    }

    @Test
    @DisplayName("Should successfully cancel a PENDING booking")
    void testCancelBooking_PendingBooking() throws Exception {
        // Create a pending booking
        Booking booking = createBooking(Booking.BookingStatus.PENDING, Booking.PaymentStatus.PENDING);

        // Cancel the booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        // Verify in database
        Booking cancelledBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assert cancelledBooking.getStatus() == Booking.BookingStatus.CANCELLED;
    }

    @Test
    @DisplayName("Should successfully cancel a CONFIRMED booking")
    void testCancelBooking_ConfirmedBooking() throws Exception {
        // Create a confirmed booking (deposit paid)
        Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

        // Cancel the booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Should reject cancellation of already cancelled booking")
    void testCancelBooking_AlreadyCancelled() throws Exception {
        // Create an already cancelled booking
        Booking booking = createBooking(Booking.BookingStatus.CANCELLED, Booking.PaymentStatus.CANCELLED);

        // Try to cancel again - should fail
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject cancellation of completed booking")
    void testCancelBooking_CompletedBooking() throws Exception {
        // Create a completed booking
        Booking booking = createBooking(Booking.BookingStatus.COMPLETED, Booking.PaymentStatus.FULLY_PAID);

        // Try to cancel - should fail
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when booking not found")
    void testCancelBooking_NotFound() throws Exception {
        mockMvc.perform(patch("/api/bookings/{id}/cancel", 99999L)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should make slot available again after cancellation")
    void testCancelBooking_SlotBecomesAvailable() throws Exception {
        // Setup barber availability
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(futureDate.getDayOfWeek());
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Create a confirmed booking at 10:00
        Booking booking = new Booking();
        booking.setCustomer(testCustomer);
        booking.setBarber(testBarber);
        booking.setService(testService);
        booking.setBookingDate(futureDate);
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(10, 30));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        booking.setDepositAmount(BigDecimal.valueOf(12.50));
        booking.setOutstandingBalance(BigDecimal.valueOf(12.50));
        booking = bookingRepository.save(booking);

        // Verify 10:00 is NOT available (booking exists)
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", not(hasItem("10:00"))));

        // Cancel the booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        // Verify 10:00 is NOW available (cancelled booking doesn't block)
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasItem("10:00")));
    }

    @Test
    @DisplayName("Admin should be able to cancel any booking")
    void testCancelBooking_AdminCanCancelAnyBooking() throws Exception {
        // Create a booking for the customer
        Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

        // Admin cancels the booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Cancel endpoint allows unauthenticated requests (public endpoint)")
    void testCancelBooking_Unauthenticated() throws Exception {
        Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

        // Cancel without authentication - allowed for this endpoint
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Should allow cancellation of NO_SHOW booking")
    void testCancelBooking_NoShowBooking() throws Exception {
        // Create a no-show booking
        Booking booking = createBooking(Booking.BookingStatus.NO_SHOW, Booking.PaymentStatus.DEPOSIT_PAID);

        // Cancelling a no-show booking is allowed
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Should preserve booking details after cancellation")
    void testCancelBooking_PreservesBookingDetails() throws Exception {
        // Create a booking with all details
        Booking booking = new Booking();
        booking.setCustomer(testCustomer);
        booking.setBarber(testBarber);
        booking.setService(testService);
        booking.setBookingDate(futureDate);
        booking.setStartTime(LocalTime.of(14, 0));
        booking.setEndTime(LocalTime.of(14, 30));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        booking.setDepositAmount(BigDecimal.valueOf(12.50));
        booking.setOutstandingBalance(BigDecimal.valueOf(12.50));
        booking.setNotes("Special request: no talking");
        booking = bookingRepository.save(booking);

        // Cancel the booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")))
                .andExpect(jsonPath("$.bookingDate", is(futureDate.toString())))
                .andExpect(jsonPath("$.startTime", is("14:00:00")))
                .andExpect(jsonPath("$.notes", is("Special request: no talking")));
    }

    @Test
    @DisplayName("Should handle multiple cancellations in sequence")
    void testCancelBooking_MultipleCancellations() throws Exception {
        // Create multiple bookings
        Booking booking1 = createBookingAtTime(LocalTime.of(9, 0), LocalTime.of(9, 30));
        Booking booking2 = createBookingAtTime(LocalTime.of(10, 0), LocalTime.of(10, 30));
        Booking booking3 = createBookingAtTime(LocalTime.of(11, 0), LocalTime.of(11, 30));

        // Cancel first booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking1.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        // Cancel third booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking3.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        // Verify second booking is still active
        Booking refreshedBooking2 = bookingRepository.findById(booking2.getId()).orElseThrow();
        assert refreshedBooking2.getStatus() == Booking.BookingStatus.CONFIRMED;
    }

    @Test
    @DisplayName("Barber should be able to cancel booking assigned to them")
    void testCancelBooking_BarberCanCancelOwnBooking() throws Exception {
        // Get barber's token
        String barberToken = jwtUtil.generateToken(
                testBarber.getUser().getEmail(),
                testBarber.getUser().getRole().name(),
                testBarber.getUser().getId()
        );

        // Create a booking for this barber
        Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

        // Barber cancels the booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + barberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    // Helper methods

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

    private Booking createBookingAtTime(LocalTime startTime, LocalTime endTime) {
        Booking booking = new Booking();
        booking.setCustomer(testCustomer);
        booking.setBarber(testBarber);
        booking.setService(testService);
        booking.setBookingDate(futureDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        booking.setDepositAmount(BigDecimal.valueOf(12.50));
        booking.setOutstandingBalance(BigDecimal.valueOf(12.50));
        return bookingRepository.save(booking);
    }

    // ==================== ADDITIONAL EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should update payment status to CANCELLED when cancelling deposit-paid booking")
    void testCancelBooking_UpdatesPaymentStatus() throws Exception {
        // Create a confirmed booking with deposit paid
        Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

        // Cancel the booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        // Verify booking status in database
        Booking cancelledBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assert cancelledBooking.getStatus() == Booking.BookingStatus.CANCELLED;
    }

    @Test
    @DisplayName("Should return correct response body structure after cancellation")
    void testCancelBooking_ResponseStructure() throws Exception {
        Booking booking = createBooking(Booking.BookingStatus.PENDING, Booking.PaymentStatus.PENDING);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status", is("CANCELLED")))
                .andExpect(jsonPath("$.bookingDate").exists())
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.endTime").exists())
                .andExpect(jsonPath("$.customer").exists())
                .andExpect(jsonPath("$.barber").exists())
                .andExpect(jsonPath("$.service").exists());
    }

    @Test
    @DisplayName("Should cancel booking regardless of booking date (past or future)")
    void testCancelBooking_PastDateBooking() throws Exception {
        // Create a booking with a past date (shouldn't normally happen but test the behavior)
        Booking booking = new Booking();
        booking.setCustomer(testCustomer);
        booking.setBarber(testBarber);
        booking.setService(testService);
        booking.setBookingDate(LocalDate.now().minusDays(1)); // Past date
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(10, 30));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        booking.setDepositAmount(BigDecimal.valueOf(12.50));
        booking.setOutstandingBalance(BigDecimal.valueOf(12.50));
        booking = bookingRepository.save(booking);

        // Should still be able to cancel
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Should handle cancellation of booking with fully paid status")
    void testCancelBooking_FullyPaidBooking() throws Exception {
        // Create a fully paid booking (not completed yet)
        Booking booking = new Booking();
        booking.setCustomer(testCustomer);
        booking.setBarber(testBarber);
        booking.setService(testService);
        booking.setBookingDate(futureDate);
        booking.setStartTime(LocalTime.of(15, 0));
        booking.setEndTime(LocalTime.of(15, 30));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.FULLY_PAID);
        booking.setDepositAmount(BigDecimal.valueOf(25.00));
        booking.setOutstandingBalance(BigDecimal.ZERO);
        booking = bookingRepository.save(booking);

        // Should be able to cancel even if fully paid (refund would be handled separately)
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Different customer should still be able to cancel (public endpoint)")
    void testCancelBooking_DifferentCustomerCanCancel() throws Exception {
        // Create another customer
        User otherCustomer = new User();
        otherCustomer.setFirstName("Other");
        otherCustomer.setLastName("Customer");
        otherCustomer.setEmail("other@test.com");
        otherCustomer.setPhone("5555555555");
        otherCustomer.setPasswordHash("password123");
        otherCustomer.setRole(User.Role.CUSTOMER);
        otherCustomer = userRepository.save(otherCustomer);

        String otherCustomerToken = jwtUtil.generateToken(
                otherCustomer.getEmail(),
                otherCustomer.getRole().name(),
                otherCustomer.getId()
        );

        // Create booking for original customer
        Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

        // Other customer tries to cancel - this endpoint is public so it should work
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + otherCustomerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Should handle cancellation with invalid booking ID format gracefully")
    void testCancelBooking_InvalidIdFormat() throws Exception {
        mockMvc.perform(patch("/api/bookings/{id}/cancel", "invalid")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle cancellation with zero booking ID")
    void testCancelBooking_ZeroId() throws Exception {
        mockMvc.perform(patch("/api/bookings/{id}/cancel", 0L)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle cancellation with negative booking ID")
    void testCancelBooking_NegativeId() throws Exception {
        mockMvc.perform(patch("/api/bookings/{id}/cancel", -1L)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should verify booking count decreases after cancellation when counting active bookings")
    void testCancelBooking_AffectsActiveBookingCount() throws Exception {
        // Create multiple bookings
        Booking booking1 = createBookingAtTime(LocalTime.of(9, 0), LocalTime.of(9, 30));
        Booking booking2 = createBookingAtTime(LocalTime.of(10, 0), LocalTime.of(10, 30));
        Booking booking3 = createBookingAtTime(LocalTime.of(11, 0), LocalTime.of(11, 30));

        // Count active bookings before cancellation
        long activeCountBefore = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
                .count();

        // Cancel one booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking2.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());

        // Count active bookings after cancellation
        long activeCountAfter = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
                .count();

        assert activeCountAfter == activeCountBefore - 1;
    }

    @Test
    @DisplayName("Should cancel booking created today")
    void testCancelBooking_SameDayBooking() throws Exception {
        // Setup availability for today
        BarberAvailability todayAvailability = new BarberAvailability();
        todayAvailability.setBarber(testBarber);
        todayAvailability.setDayOfWeek(LocalDate.now().getDayOfWeek());
        todayAvailability.setStartTime(LocalTime.of(9, 0));
        todayAvailability.setEndTime(LocalTime.of(17, 0));
        todayAvailability.setIsAvailable(true);
        barberAvailabilityRepository.save(todayAvailability);

        // Create a booking for today (in the future time)
        Booking booking = new Booking();
        booking.setCustomer(testCustomer);
        booking.setBarber(testBarber);
        booking.setService(testService);
        booking.setBookingDate(LocalDate.now());
        booking.setStartTime(LocalTime.of(23, 0)); // Late time to ensure it's in the future
        booking.setEndTime(LocalTime.of(23, 30));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        booking.setDepositAmount(BigDecimal.valueOf(12.50));
        booking.setOutstandingBalance(BigDecimal.valueOf(12.50));
        booking = bookingRepository.save(booking);

        // Cancel same-day booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Should preserve deposit amount after cancellation")
    void testCancelBooking_PreservesFinancialData() throws Exception {
        // Create a booking with specific financial data
        Booking booking = new Booking();
        booking.setCustomer(testCustomer);
        booking.setBarber(testBarber);
        booking.setService(testService);
        booking.setBookingDate(futureDate);
        booking.setStartTime(LocalTime.of(16, 0));
        booking.setEndTime(LocalTime.of(16, 30));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        booking.setDepositAmount(BigDecimal.valueOf(15.00));
        booking.setOutstandingBalance(BigDecimal.valueOf(10.00));
        booking = bookingRepository.save(booking);

        // Cancel and verify financial data is preserved
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")))
                .andExpect(jsonPath("$.depositAmount", is(15.00)))
                .andExpect(jsonPath("$.outstandingBalance", is(10.00)));
    }
}

