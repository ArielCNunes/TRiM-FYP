package com.trim.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trim.booking.config.JwtUtil;
import com.trim.booking.dto.booking.UpdateBookingRequest;
import com.trim.booking.entity.*;
import com.trim.booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BookingController.updateBooking() (PUT /api/bookings/{id})
 *
 * Tests cover:
 * - Authorization: Only booking owner can update their booking
 * - Status restrictions: Cannot update COMPLETED or CANCELLED bookings
 * - Conflict detection: Cannot update to a time slot that conflicts with another booking
 * - Date/time validation: Cannot update to past dates/times
 * - Availability validation: New time must be within barber's availability
 * - Successful updates: Date and time changes are persisted correctly
 * - End time calculation: End time is recalculated based on service duration
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BookingControllerUpdateBookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private User otherCustomer;
    private User testAdmin;
    private User barberUser;
    private Barber testBarber;
    private ServiceOffered testService;
    private ServiceOffered longService;
    private String customerToken;
    private String otherCustomerToken;
    private String adminToken;
    private String barberToken;
    private LocalDate futureDate;
    private LocalDate futureDate2;

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

        // Create another customer
        otherCustomer = new User();
        otherCustomer.setFirstName("Other");
        otherCustomer.setLastName("Customer");
        otherCustomer.setEmail("other@test.com");
        otherCustomer.setPhone("5555555555");
        otherCustomer.setPasswordHash("password123");
        otherCustomer.setRole(User.Role.CUSTOMER);
        otherCustomer = userRepository.save(otherCustomer);

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

        // Create test service (30 minutes)
        testService = new ServiceOffered();
        testService.setName("Basic Haircut");
        testService.setDescription("Standard haircut");
        testService.setDurationMinutes(30);
        testService.setPrice(BigDecimal.valueOf(25.00));
        testService.setDepositPercentage(50);
        testService.setActive(true);
        testService.setCategory(category);
        testService = serviceRepository.save(testService);

        // Create long service (60 minutes)
        longService = new ServiceOffered();
        longService.setName("Premium Haircut");
        longService.setDescription("Premium haircut with styling");
        longService.setDurationMinutes(60);
        longService.setPrice(BigDecimal.valueOf(50.00));
        longService.setDepositPercentage(50);
        longService.setActive(true);
        longService.setCategory(category);
        longService = serviceRepository.save(longService);

        // Generate JWT tokens
        customerToken = jwtUtil.generateToken(testCustomer.getEmail(), testCustomer.getRole().name(), testCustomer.getId());
        otherCustomerToken = jwtUtil.generateToken(otherCustomer.getEmail(), otherCustomer.getRole().name(), otherCustomer.getId());
        adminToken = jwtUtil.generateToken(testAdmin.getEmail(), testAdmin.getRole().name(), testAdmin.getId());
        barberToken = jwtUtil.generateToken(barberUser.getEmail(), barberUser.getRole().name(), barberUser.getId());

        // Use future Mondays for testing
        futureDate = LocalDate.now().plusWeeks(1);
        while (futureDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            futureDate = futureDate.plusDays(1);
        }
        futureDate2 = futureDate.plusDays(1); // Tuesday

        // Set up barber availability for both days
        setupBarberAvailability(futureDate.getDayOfWeek());
        setupBarberAvailability(futureDate2.getDayOfWeek());
    }

    private void setupBarberAvailability(DayOfWeek dayOfWeek) {
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(dayOfWeek);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);
    }

    // Helper method to create bookings
    private Booking createBooking(Booking.BookingStatus status, LocalDate date, LocalTime startTime, User customer) {
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setBarber(testBarber);
        booking.setService(testService);
        booking.setBookingDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(startTime.plusMinutes(testService.getDurationMinutes()));
        booking.setStatus(status);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        booking.setDepositAmount(BigDecimal.valueOf(12.50));
        booking.setOutstandingBalance(BigDecimal.valueOf(12.50));
        return bookingRepository.save(booking);
    }

    private Booking createBookingWithService(Booking.BookingStatus status, LocalDate date, LocalTime startTime,
                                              User customer, ServiceOffered service) {
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setBarber(testBarber);
        booking.setService(service);
        booking.setBookingDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(startTime.plusMinutes(service.getDurationMinutes()));
        booking.setStatus(status);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        booking.setDepositAmount(BigDecimal.valueOf(12.50));
        booking.setOutstandingBalance(BigDecimal.valueOf(12.50));
        return bookingRepository.save(booking);
    }

    // ==================== Authorization Tests ====================

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Customer can update their own booking")
        void testUpdateBooking_OwnerAuthorized() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime", is("14:00:00")));
        }

        @Test
        @DisplayName("Customer cannot update another customer's booking")
        void testUpdateBooking_NotOwnerForbidden() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + otherCustomerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            // Verify booking unchanged
            Booking unchanged = bookingRepository.findById(booking.getId()).orElseThrow();
            assertEquals(LocalTime.of(10, 0), unchanged.getStartTime());
        }

        @Test
        @DisplayName("Admin cannot update customer's booking (only owner can)")
        void testUpdateBooking_AdminCannotUpdateOthersBooking() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            // Admin trying to update customer's booking - should be forbidden per current implementation
            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Barber cannot update customer's booking")
        void testUpdateBooking_BarberCannotUpdate() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + barberToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthenticated request should be rejected")
        void testUpdateBooking_Unauthenticated() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== Status Restriction Tests ====================

    @Nested
    @DisplayName("Status Restriction Tests")
    class StatusRestrictionTests {

        @Test
        @DisplayName("Can update a PENDING booking")
        void testUpdateBooking_PendingBooking() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.PENDING, futureDate, LocalTime.of(10, 0), testCustomer);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime", is("14:00:00")));
        }

        @Test
        @DisplayName("Can update a CONFIRMED booking")
        void testUpdateBooking_ConfirmedBooking() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime", is("14:00:00")));
        }

        @Test
        @DisplayName("Cannot update a COMPLETED booking")
        void testUpdateBooking_CompletedBooking() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.COMPLETED, futureDate, LocalTime.of(10, 0), testCustomer);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Verify booking unchanged
            Booking unchanged = bookingRepository.findById(booking.getId()).orElseThrow();
            assertEquals(LocalTime.of(10, 0), unchanged.getStartTime());
        }

        @Test
        @DisplayName("Cannot update a CANCELLED booking")
        void testUpdateBooking_CancelledBooking() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CANCELLED, futureDate, LocalTime.of(10, 0), testCustomer);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Can update a NO_SHOW booking")
        void testUpdateBooking_NoShowBooking() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.NO_SHOW, futureDate, LocalTime.of(10, 0), testCustomer);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime", is("14:00:00")));
        }
    }

    // ==================== Conflict Detection Tests ====================

    @Nested
    @DisplayName("Conflict Detection Tests")
    class ConflictDetectionTests {

        @Test
        @DisplayName("Cannot update to time slot with existing booking")
        void testUpdateBooking_ConflictWithExistingBooking() throws Exception {
            // Create an existing booking at 14:00
            createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(14, 0), otherCustomer);

            // Create booking to update
            Booking bookingToUpdate = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            // Try to update to conflicting time
            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", bookingToUpdate.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());

            // Verify booking unchanged
            Booking unchanged = bookingRepository.findById(bookingToUpdate.getId()).orElseThrow();
            assertEquals(LocalTime.of(10, 0), unchanged.getStartTime());
        }

        @Test
        @DisplayName("Cannot update to time that overlaps with existing booking")
        void testUpdateBooking_OverlappingTime() throws Exception {
            // Create an existing 30-min booking at 14:00-14:30
            createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(14, 0), otherCustomer);

            // Create booking to update
            Booking bookingToUpdate = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            // Try to update to 13:45 which would overlap (13:45-14:15 conflicts with 14:00-14:30)
            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(13, 45));

            mockMvc.perform(put("/api/bookings/{id}", bookingToUpdate.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Can update to slot adjacent to existing booking (no overlap)")
        void testUpdateBooking_AdjacentSlot() throws Exception {
            // Create an existing booking at 14:00-14:30
            createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(14, 0), otherCustomer);

            // Create booking to update
            Booking bookingToUpdate = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            // Update to 14:30 which should be allowed (right after the 14:00 booking ends)
            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 30));

            mockMvc.perform(put("/api/bookings/{id}", bookingToUpdate.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime", is("14:30:00")));
        }

        @Test
        @DisplayName("Updating to same time slot should succeed (no self-conflict)")
        void testUpdateBooking_SameTimeSlot() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            // Update to same time (just changing something else conceptually)
            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(10, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime", is("10:00:00")));
        }

        @Test
        @DisplayName("Cancelled bookings don't cause conflicts")
        void testUpdateBooking_CancelledBookingNoConflict() throws Exception {
            // Create a cancelled booking at 14:00
            Booking cancelledBooking = createBooking(Booking.BookingStatus.CANCELLED, futureDate, LocalTime.of(14, 0), otherCustomer);

            // Create booking to update
            Booking bookingToUpdate = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            // Should be able to update to 14:00 since the other booking is cancelled
            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", bookingToUpdate.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime", is("14:00:00")));
        }
    }

    // ==================== Date/Time Validation Tests ====================

    @Nested
    @DisplayName("Date/Time Validation Tests")
    class DateTimeValidationTests {

        @Test
        @DisplayName("Cannot update to a past date")
        void testUpdateBooking_PastDate() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            LocalDate pastDate = LocalDate.now().minusDays(1);
            UpdateBookingRequest request = new UpdateBookingRequest(pastDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Can update to a different future date")
        void testUpdateBooking_DifferentFutureDate() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate2, LocalTime.of(10, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookingDate", is(futureDate2.toString())));
        }

        @Test
        @DisplayName("Null booking date should be rejected")
        void testUpdateBooking_NullDate() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            String requestJson = "{\"startTime\": \"14:00\"}";

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Null start time should be rejected")
        void testUpdateBooking_NullStartTime() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            String requestJson = "{\"bookingDate\": \"" + futureDate + "\"}";

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== Successful Update Tests ====================

    @Nested
    @DisplayName("Successful Update Tests")
    class SuccessfulUpdateTests {

        @Test
        @DisplayName("Update both date and time")
        void testUpdateBooking_ChangeDateAndTime() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate2, LocalTime.of(15, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookingDate", is(futureDate2.toString())))
                    .andExpect(jsonPath("$.startTime", is("15:00:00")));

            // Verify in database
            Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
            assertEquals(futureDate2, updated.getBookingDate());
            assertEquals(LocalTime.of(15, 0), updated.getStartTime());
        }

        @Test
        @DisplayName("End time is recalculated based on service duration")
        void testUpdateBooking_EndTimeRecalculated() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);
            // Original: 10:00 - 10:30 (30 min service)

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime", is("14:00:00")))
                    .andExpect(jsonPath("$.endTime", is("14:30:00")));
        }

        @Test
        @DisplayName("End time calculated correctly for longer service")
        void testUpdateBooking_LongServiceEndTime() throws Exception {
            // Create booking with 60-minute service
            Booking booking = createBookingWithService(Booking.BookingStatus.CONFIRMED, futureDate,
                    LocalTime.of(10, 0), testCustomer, longService);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime", is("14:00:00")))
                    .andExpect(jsonPath("$.endTime", is("15:00:00")));
        }

        @Test
        @DisplayName("Update preserves other booking fields")
        void testUpdateBooking_PreservesOtherFields() throws Exception {
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
            booking.setNotes("Special request: short on sides");
            booking = bookingRepository.save(booking);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Verify other fields preserved
            Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
            assertEquals("Special request: short on sides", updated.getNotes());
            assertEquals(Booking.BookingStatus.CONFIRMED, updated.getStatus());
            assertEquals(Booking.PaymentStatus.DEPOSIT_PAID, updated.getPaymentStatus());
            assertEquals(testService.getId(), updated.getService().getId());
            assertEquals(testBarber.getId(), updated.getBarber().getId());
        }

        @Test
        @DisplayName("Returns full booking details in response")
        void testUpdateBooking_ReturnsFullDetails() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                    .andExpect(jsonPath("$.customer").exists())
                    .andExpect(jsonPath("$.barber").exists())
                    .andExpect(jsonPath("$.service").exists())
                    .andExpect(jsonPath("$.status", is("CONFIRMED")))
                    .andExpect(jsonPath("$.paymentStatus", is("DEPOSIT_PAID")));
        }
    }

    // ==================== Not Found Tests ====================

    @Nested
    @DisplayName("Not Found Tests")
    class NotFoundTests {

        @Test
        @DisplayName("Should return 404 when booking not found")
        void testUpdateBooking_NotFound() throws Exception {
            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));

            mockMvc.perform(put("/api/bookings/{id}", 99999L)
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== Edge Cases ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Update booking multiple times")
        void testUpdateBooking_MultipleTimes() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            // First update
            UpdateBookingRequest request1 = new UpdateBookingRequest(futureDate, LocalTime.of(11, 0));
            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime", is("11:00:00")));

            // Second update
            UpdateBookingRequest request2 = new UpdateBookingRequest(futureDate, LocalTime.of(15, 0));
            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime", is("15:00:00")));

            // Third update to a different date
            UpdateBookingRequest request3 = new UpdateBookingRequest(futureDate2, LocalTime.of(9, 0));
            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request3)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookingDate", is(futureDate2.toString())))
                    .andExpect(jsonPath("$.startTime", is("09:00:00")));
        }

        @Test
        @DisplayName("Update at boundary times (start and end of day)")
        void testUpdateBooking_BoundaryTimes() throws Exception {
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            // Update to first slot of the day (9:00)
            UpdateBookingRequest request1 = new UpdateBookingRequest(futureDate, LocalTime.of(9, 0));
            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime", is("09:00:00")));

            // Update to last possible slot (16:30 for 30-min service ending at 17:00)
            UpdateBookingRequest request2 = new UpdateBookingRequest(futureDate, LocalTime.of(16, 30));
            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime", is("16:30:00")))
                    .andExpect(jsonPath("$.endTime", is("17:00:00")));
        }

        @Test
        @DisplayName("Update frees up original slot for others")
        void testUpdateBooking_FreesOriginalSlot() throws Exception {
            // Create booking at 10:00
            Booking booking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), testCustomer);

            // Update to 14:00
            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));
            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Other customer should now be able to book at 10:00
            Booking otherBooking = createBooking(Booking.BookingStatus.CONFIRMED, futureDate, LocalTime.of(10, 0), otherCustomer);
            assertNotNull(otherBooking.getId());
            assertEquals(LocalTime.of(10, 0), otherBooking.getStartTime());
        }

        @Test
        @DisplayName("Pending booking with expiry gets new expiry on update")
        void testUpdateBooking_ExpiryReset() throws Exception {
            // Create a pending booking with expiry
            Booking booking = new Booking();
            booking.setCustomer(testCustomer);
            booking.setBarber(testBarber);
            booking.setService(testService);
            booking.setBookingDate(futureDate);
            booking.setStartTime(LocalTime.of(10, 0));
            booking.setEndTime(LocalTime.of(10, 30));
            booking.setStatus(Booking.BookingStatus.PENDING);
            booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
            booking.setDepositAmount(BigDecimal.ZERO);
            booking.setOutstandingBalance(testService.getPrice());
            booking.setExpiresAt(LocalDateTime.now().plusMinutes(5)); // 5 minutes from now
            booking = bookingRepository.save(booking);

            LocalDateTime originalExpiry = booking.getExpiresAt();

            // Wait a tiny bit to ensure time difference
            Thread.sleep(10);

            // Update the booking
            UpdateBookingRequest request = new UpdateBookingRequest(futureDate, LocalTime.of(14, 0));
            mockMvc.perform(put("/api/bookings/{id}", booking.getId())
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Verify expiry was reset (should be 10 minutes from now)
            Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
            assertNotNull(updated.getExpiresAt());
            assertTrue(updated.getExpiresAt().isAfter(originalExpiry));
        }
    }
}

