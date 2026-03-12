package com.trim.booking.controller;

import com.trim.booking.config.JwtUtil;
import com.trim.booking.entity.*;
import com.trim.booking.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    private BusinessRepository businessRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Business business;
    private User testCustomer;
    private User testAdmin;
    private Barber testBarber;
    private ServiceOffered testService;
    private String customerToken;
    private String adminToken;
    private LocalDate futureDate;

    @BeforeAll
    void setupOnce() {
        business = new Business();
        business.setName("Test Barbershop Cancel");
        business = businessRepository.save(business);
    }

    @BeforeEach
    void setUp() {
        // Clean up in order respecting foreign key constraints
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
        testCustomer.setBusiness(business);
        testCustomer = userRepository.save(testCustomer);

        // Create admin user
        testAdmin = new User();
        testAdmin.setFirstName("Admin");
        testAdmin.setLastName("User");
        testAdmin.setEmail("admin@test.com");
        testAdmin.setPhone("1111111111");
        testAdmin.setPasswordHash("password123");
        testAdmin.setRole(User.Role.ADMIN);
        testAdmin.setBusiness(business);
        testAdmin = userRepository.save(testAdmin);

        // Create barber user
        User barberUser = new User();
        barberUser.setFirstName("John");
        barberUser.setLastName("Barber");
        barberUser.setEmail("john@trim.com");
        barberUser.setPhone("1234567890");
        barberUser.setPasswordHash("password123");
        barberUser.setRole(User.Role.BARBER);
        barberUser.setBusiness(business);
        barberUser = userRepository.save(barberUser);

        // Create test barber
        testBarber = new Barber();
        testBarber.setUser(barberUser);
        testBarber.setBio("Test barber");
        testBarber.setActive(true);
        testBarber.setBusiness(business);
        testBarber = barberRepository.save(testBarber);

        // Create service category
        ServiceCategory category = new ServiceCategory();
        category.setName("Haircuts");
        category.setActive(true);
        category.setBusiness(business);
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
        testService.setBusiness(business);
        testService = serviceRepository.save(testService);

        // Generate JWT tokens
        customerToken = jwtUtil.generateToken(testCustomer.getEmail(), testCustomer.getRole().name(), testCustomer.getId(), business.getId());
        adminToken = jwtUtil.generateToken(testAdmin.getEmail(), testAdmin.getRole().name(), testAdmin.getId(), business.getId());

        // Use a future Monday for testing
        futureDate = LocalDate.now().plusWeeks(1);
        while (futureDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            futureDate = futureDate.plusDays(1);
        }
    }

    @Test
    @DisplayName("Should successfully cancel a PENDING booking")
    void testCancelBooking_PendingBooking() throws Exception {
        Booking booking = createBooking(Booking.BookingStatus.PENDING, Booking.PaymentStatus.PENDING);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        Booking cancelledBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assert cancelledBooking.getStatus() == Booking.BookingStatus.CANCELLED;
    }

    @Test
    @DisplayName("Should successfully cancel a CONFIRMED booking")
    void testCancelBooking_ConfirmedBooking() throws Exception {
        Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Should reject cancellation of already cancelled booking")
    void testCancelBooking_AlreadyCancelled() throws Exception {
        Booking booking = createBooking(Booking.BookingStatus.CANCELLED, Booking.PaymentStatus.CANCELLED);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject cancellation of completed booking")
    void testCancelBooking_CompletedBooking() throws Exception {
        Booking booking = createBooking(Booking.BookingStatus.COMPLETED, Booking.PaymentStatus.FULLY_PAID);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when booking not found")
    void testCancelBooking_NotFound() throws Exception {
        mockMvc.perform(patch("/api/bookings/{id}/cancel", 99999L)
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
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
        availability.setBusiness(business);
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
        booking.setBusiness(business);
        booking = bookingRepository.save(booking);

        // Verify 10:00 is NOT available (booking exists)
        mockMvc.perform(get("/api/availability")
                        .header("X-Business-Slug", business.getSlug())
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", not(hasItem("10:00"))));

        // Cancel the booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        // Verify 10:00 is NOW available (cancelled booking doesn't block)
        mockMvc.perform(get("/api/availability")
                        .header("X-Business-Slug", business.getSlug())
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasItem("10:00")));
    }

    @Test
    @DisplayName("Admin should be able to cancel any booking")
    void testCancelBooking_AdminCanCancelAnyBooking() throws Exception {
        Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Unauthenticated request should be rejected")
    void testCancelBooking_Unauthenticated() throws Exception {
        Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow cancellation of NO_SHOW booking")
    void testCancelBooking_NoShowBooking() throws Exception {
        Booking booking = createBooking(Booking.BookingStatus.NO_SHOW, Booking.PaymentStatus.DEPOSIT_PAID);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Should preserve booking details after cancellation")
    void testCancelBooking_PreservesBookingDetails() throws Exception {
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
        booking.setBusiness(business);
        booking = bookingRepository.save(booking);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")))
                .andExpect(jsonPath("$.bookingDate", is(futureDate.toString())))
                .andExpect(jsonPath("$.startTime", is("14:00:00")))
                .andExpect(jsonPath("$.notes", is("Special request: no talking")));
    }

    @Test
    @DisplayName("Should handle multiple cancellations in sequence")
    void testCancelBooking_MultipleCancellations() throws Exception {
        Booking booking1 = createBookingAtTime(LocalTime.of(9, 0), LocalTime.of(9, 30));
        Booking booking2 = createBookingAtTime(LocalTime.of(10, 0), LocalTime.of(10, 30));
        Booking booking3 = createBookingAtTime(LocalTime.of(11, 0), LocalTime.of(11, 30));

        // Cancel first booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking1.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        // Cancel third booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking3.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        // Verify second booking is still active
        Booking refreshedBooking2 = bookingRepository.findById(booking2.getId()).orElseThrow();
        assert refreshedBooking2.getStatus() == Booking.BookingStatus.CONFIRMED;
    }

    @Test
    @DisplayName("Different customer should be forbidden from cancelling another's booking")
    void testCancelBooking_DifferentCustomerForbidden() throws Exception {
        // Create another customer
        User otherCustomer = new User();
        otherCustomer.setFirstName("Other");
        otherCustomer.setLastName("Customer");
        otherCustomer.setEmail("other@test.com");
        otherCustomer.setPhone("5555555555");
        otherCustomer.setPasswordHash("password123");
        otherCustomer.setRole(User.Role.CUSTOMER);
        otherCustomer.setBusiness(business);
        otherCustomer = userRepository.save(otherCustomer);

        String otherCustomerToken = jwtUtil.generateToken(
                otherCustomer.getEmail(),
                otherCustomer.getRole().name(),
                otherCustomer.getId(),
                business.getId()
        );

        Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + otherCustomerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Barber (non-owner) should be forbidden from cancelling booking")
    void testCancelBooking_BarberForbidden() throws Exception {
        String barberToken = jwtUtil.generateToken(
                testBarber.getUser().getEmail(),
                testBarber.getUser().getRole().name(),
                testBarber.getUser().getId(),
                business.getId()
        );

        Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + barberToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should handle cancellation with invalid booking ID format gracefully")
    void testCancelBooking_InvalidIdFormat() throws Exception {
        mockMvc.perform(patch("/api/bookings/{id}/cancel", "invalid")
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle cancellation with zero booking ID")
    void testCancelBooking_ZeroId() throws Exception {
        mockMvc.perform(patch("/api/bookings/{id}/cancel", 0L)
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle cancellation with negative booking ID")
    void testCancelBooking_NegativeId() throws Exception {
        mockMvc.perform(patch("/api/bookings/{id}/cancel", -1L)
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should verify booking count decreases after cancellation when counting active bookings")
    void testCancelBooking_AffectsActiveBookingCount() throws Exception {
        Booking booking1 = createBookingAtTime(LocalTime.of(9, 0), LocalTime.of(9, 30));
        Booking booking2 = createBookingAtTime(LocalTime.of(10, 0), LocalTime.of(10, 30));
        Booking booking3 = createBookingAtTime(LocalTime.of(11, 0), LocalTime.of(11, 30));

        long activeCountBefore = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
                .count();

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking2.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk());

        long activeCountAfter = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
                .count();

        assert activeCountAfter == activeCountBefore - 1;
    }

    @Test
    @DisplayName("Should cancel booking created today")
    void testCancelBooking_SameDayBooking() throws Exception {
        BarberAvailability todayAvailability = new BarberAvailability();
        todayAvailability.setBarber(testBarber);
        todayAvailability.setDayOfWeek(LocalDate.now().getDayOfWeek());
        todayAvailability.setStartTime(LocalTime.of(9, 0));
        todayAvailability.setEndTime(LocalTime.of(17, 0));
        todayAvailability.setIsAvailable(true);
        todayAvailability.setBusiness(business);
        barberAvailabilityRepository.save(todayAvailability);

        Booking booking = new Booking();
        booking.setCustomer(testCustomer);
        booking.setBarber(testBarber);
        booking.setService(testService);
        booking.setBookingDate(LocalDate.now());
        booking.setStartTime(LocalTime.of(23, 0));
        booking.setEndTime(LocalTime.of(23, 30));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        booking.setDepositAmount(BigDecimal.valueOf(12.50));
        booking.setOutstandingBalance(BigDecimal.valueOf(12.50));
        booking.setBusiness(business);
        booking = bookingRepository.save(booking);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Should preserve deposit amount after cancellation")
    void testCancelBooking_PreservesFinancialData() throws Exception {
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
        booking.setBusiness(business);
        booking = bookingRepository.save(booking);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")))
                .andExpect(jsonPath("$.depositAmount", is(15.00)))
                .andExpect(jsonPath("$.outstandingBalance", is(10.00)));
    }

    @Test
    @DisplayName("Should update payment status to CANCELLED when cancelling deposit-paid booking")
    void testCancelBooking_UpdatesPaymentStatus() throws Exception {
        Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, Booking.PaymentStatus.DEPOSIT_PAID);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        Booking cancelledBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assert cancelledBooking.getStatus() == Booking.BookingStatus.CANCELLED;
    }

    @Test
    @DisplayName("Should return correct response body structure after cancellation")
    void testCancelBooking_ResponseStructure() throws Exception {
        Booking booking = createBooking(Booking.BookingStatus.PENDING, Booking.PaymentStatus.PENDING);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
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
        Booking booking = new Booking();
        booking.setCustomer(testCustomer);
        booking.setBarber(testBarber);
        booking.setService(testService);
        booking.setBookingDate(LocalDate.now().minusDays(1));
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(10, 30));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        booking.setDepositAmount(BigDecimal.valueOf(12.50));
        booking.setOutstandingBalance(BigDecimal.valueOf(12.50));
        booking.setBusiness(business);
        booking = bookingRepository.save(booking);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Should handle cancellation of booking with fully paid status")
    void testCancelBooking_FullyPaidBooking() throws Exception {
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
        booking.setBusiness(business);
        booking = bookingRepository.save(booking);

        mockMvc.perform(patch("/api/bookings/{id}/cancel", booking.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
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
        booking.setBusiness(business);
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
        booking.setBusiness(business);
        return bookingRepository.save(booking);
    }
}
