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
}

