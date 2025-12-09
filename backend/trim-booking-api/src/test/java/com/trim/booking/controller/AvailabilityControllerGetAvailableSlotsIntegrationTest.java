package com.trim.booking.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AvailabilityController.getAvailableSlots()
 *
 * Tests the complex availability calculation logic that considers:
 * - Barber working hours by day of week
 * - Existing bookings
 * - Barber breaks
 * - Service duration
 * - Past time filtering
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AvailabilityControllerGetAvailableSlotsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private BarberAvailabilityRepository barberAvailabilityRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BarberBreakRepository barberBreakRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    private Barber testBarber;
    private ServiceOffered testService;
    private User testCustomer;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        // Clean up
        bookingRepository.deleteAll();
        barberBreakRepository.deleteAll();
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
        testBarber.setBio("Specializes in haircuts");
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

        // Use a future Monday for testing (to avoid today's time filtering issues)
        futureDate = LocalDate.now().plusWeeks(1);
        while (futureDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            futureDate = futureDate.plusDays(1);
        }
    }

    @Test
    @DisplayName("Should return available slots when barber has no bookings")
    void testGetAvailableSlots_NoBookings() throws Exception {
        // Setup: Barber works Monday 9:00-17:00
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Execute & Verify: Should return slots from 9:00 to 16:30 (last slot that allows 30min service)
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(20)))) // Should have many slots
                .andExpect(jsonPath("$[0]", is("09:00"))) // First slot
                .andExpect(jsonPath("$[1]", is("09:15"))) // 15-minute intervals
                .andExpect(jsonPath("$[*]", hasItem("16:30"))) // Last possible slot
                .andExpect(jsonPath("$[*]", not(hasItem("16:45")))); // Not enough time for service
    }

    @Test
    @DisplayName("Should exclude slots occupied by existing bookings")
    void testGetAvailableSlots_WithExistingBooking() throws Exception {
        // Setup: Barber works Monday 9:00-17:00
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Create existing booking at 10:00-10:30
        Booking existingBooking = new Booking();
        existingBooking.setCustomer(testCustomer);
        existingBooking.setBarber(testBarber);
        existingBooking.setService(testService);
        existingBooking.setBookingDate(futureDate);
        existingBooking.setStartTime(LocalTime.of(10, 0));
        existingBooking.setEndTime(LocalTime.of(10, 30));
        existingBooking.setStatus(Booking.BookingStatus.CONFIRMED);
        existingBooking.setPaymentStatus(Booking.PaymentStatus.FULLY_PAID);
        existingBooking.setDepositAmount(testService.getPrice());
        existingBooking.setOutstandingBalance(BigDecimal.ZERO);
        existingBooking.setCreatedAt(LocalDateTime.now());
        bookingRepository.save(existingBooking);

        // Execute & Verify: Should not include slots that would overlap with 10:00-10:30 booking
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasItem("09:30"))) // 09:30-10:00 fits before booking
                .andExpect(jsonPath("$[*]", not(hasItem("09:45")))) // 09:45-10:15 would overlap
                .andExpect(jsonPath("$[*]", not(hasItem("10:00")))) // Conflicts
                .andExpect(jsonPath("$[*]", not(hasItem("10:15")))) // Would end at 10:45, overlaps
                .andExpect(jsonPath("$[*]", hasItem("10:30"))); // After booking ends
    }

    @Test
    @DisplayName("Should include slots where cancelled bookings exist")
    void testGetAvailableSlots_CancelledBookingDoesNotBlock() throws Exception {
        // Setup: Barber works Monday 9:00-17:00
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Create cancelled booking at 10:00-10:30
        Booking cancelledBooking = new Booking();
        cancelledBooking.setCustomer(testCustomer);
        cancelledBooking.setBarber(testBarber);
        cancelledBooking.setService(testService);
        cancelledBooking.setBookingDate(futureDate);
        cancelledBooking.setStartTime(LocalTime.of(10, 0));
        cancelledBooking.setEndTime(LocalTime.of(10, 30));
        cancelledBooking.setStatus(Booking.BookingStatus.CANCELLED);
        cancelledBooking.setPaymentStatus(Booking.PaymentStatus.CANCELLED);
        cancelledBooking.setDepositAmount(BigDecimal.ZERO);
        cancelledBooking.setOutstandingBalance(BigDecimal.ZERO);
        cancelledBooking.setCreatedAt(LocalDateTime.now());
        bookingRepository.save(cancelledBooking);

        // Execute & Verify: Should include 10:00 slot (cancelled booking doesn't block)
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasItem("10:00"))) // Available despite cancelled booking
                .andExpect(jsonPath("$[*]", hasItem("10:15")));
    }

    @Test
    @DisplayName("Should exclude slots that conflict with barber breaks")
    void testGetAvailableSlots_WithBarberBreak() throws Exception {
        // Setup: Barber works Monday 9:00-17:00
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Create break from 12:00-13:00 (lunch)
        BarberBreak lunchBreak = new BarberBreak();
        lunchBreak.setBarber(testBarber);
        lunchBreak.setLabel("Lunch Break");
        lunchBreak.setStartTime(LocalTime.of(12, 0));
        lunchBreak.setEndTime(LocalTime.of(13, 0));
        barberBreakRepository.save(lunchBreak);

        // Execute & Verify: 11:30-12:00 fits, but 11:45-12:15 would overlap with lunch
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasItem("11:30"))) // 11:30-12:00 fits exactly before break
                .andExpect(jsonPath("$[*]", not(hasItem("11:45")))) // 11:45-12:15 would overlap break
                .andExpect(jsonPath("$[*]", not(hasItem("12:00")))) // During break
                .andExpect(jsonPath("$[*]", not(hasItem("12:15")))) // During break
                .andExpect(jsonPath("$[*]", not(hasItem("12:30")))) // 12:30-13:00 would end exactly at break end, conflicts
                .andExpect(jsonPath("$[*]", hasItem("13:00"))); // After break
    }

    @Test
    @DisplayName("Should return empty list when barber doesn't work on that day")
    void testGetAvailableSlots_BarberNotWorkingThatDay() throws Exception {
        // Setup: Barber only works Monday, not Tuesday
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Query for Tuesday
        LocalDate tuesday = futureDate.plusDays(1);

        // Execute & Verify
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", tuesday.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return empty list when barber is marked unavailable")
    void testGetAvailableSlots_BarberMarkedUnavailable() throws Exception {
        // Setup: Barber exists for Monday but is marked unavailable
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(false); // Marked unavailable
        barberAvailabilityRepository.save(availability);

        // Execute & Verify
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should adjust slots based on service duration")
    void testGetAvailableSlots_LongerService() throws Exception {
        // Create a longer service (60 minutes)
        ServiceOffered longService = new ServiceOffered();
        longService.setName("Premium Haircut");
        longService.setDescription("Extended haircut service");
        longService.setDurationMinutes(60);
        longService.setPrice(BigDecimal.valueOf(45.00));
        longService.setDepositPercentage(50);
        longService.setActive(true);
        longService.setCategory(testService.getCategory());
        longService = serviceRepository.save(longService);

        // Setup: Barber works Monday 9:00-17:00
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Execute & Verify: Last slot should be 16:00 (not 16:30) for 60-min service
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", longService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", is("09:00")))
                .andExpect(jsonPath("$[*]", hasItem("16:00"))) // Last slot for 60min service
                .andExpect(jsonPath("$[*]", not(hasItem("16:15")))) // Not enough time
                .andExpect(jsonPath("$[*]", not(hasItem("16:30"))));
    }

    @Test
    @DisplayName("Should handle multiple bookings correctly")
    void testGetAvailableSlots_MultipleBookings() throws Exception {
        // Setup: Barber works Monday 9:00-17:00
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Create multiple bookings
        createBooking(LocalTime.of(9, 0), LocalTime.of(9, 30));
        createBooking(LocalTime.of(11, 0), LocalTime.of(11, 30));
        createBooking(LocalTime.of(14, 0), LocalTime.of(14, 30));

        // Execute & Verify
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", not(hasItem("09:00")))) // Booked
                .andExpect(jsonPath("$[*]", hasItem("09:30"))) // After first booking
                .andExpect(jsonPath("$[*]", hasItem("10:30"))) // Available before second booking
                .andExpect(jsonPath("$[*]", not(hasItem("10:45")))) // 10:45-11:15 would overlap 11:00 booking
                .andExpect(jsonPath("$[*]", not(hasItem("11:00")))) // Booked
                .andExpect(jsonPath("$[*]", hasItem("11:30"))) // After second booking
                .andExpect(jsonPath("$[*]", hasItem("13:30"))) // 13:30-14:00 fits before third booking
                .andExpect(jsonPath("$[*]", not(hasItem("13:45")))) // 13:45-14:15 would overlap 14:00 booking
                .andExpect(jsonPath("$[*]", not(hasItem("14:00")))) // Booked
                .andExpect(jsonPath("$[*]", hasItem("14:30"))); // After third booking
    }

    @Test
    @DisplayName("Should handle back-to-back bookings correctly")
    void testGetAvailableSlots_BackToBackBookings() throws Exception {
        // Setup: Barber works Monday 9:00-17:00
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Create back-to-back bookings
        createBooking(LocalTime.of(10, 0), LocalTime.of(10, 30));
        createBooking(LocalTime.of(10, 30), LocalTime.of(11, 0));

        // Execute & Verify
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasItem("09:30"))) // 09:30-10:00 fits before bookings
                .andExpect(jsonPath("$[*]", not(hasItem("09:45")))) // 09:45-10:15 would overlap first booking
                .andExpect(jsonPath("$[*]", not(hasItem("10:00")))) // First booking
                .andExpect(jsonPath("$[*]", not(hasItem("10:15")))) // Would overlap
                .andExpect(jsonPath("$[*]", not(hasItem("10:30")))) // Second booking
                .andExpect(jsonPath("$[*]", hasItem("11:00"))); // After both bookings
    }

    @Test
    @DisplayName("Should handle edge case where service fits exactly in remaining time")
    void testGetAvailableSlots_ServiceFitsExactly() throws Exception {
        // Setup: Barber works Monday 9:00-10:00 (only 1 hour)
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(10, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Execute & Verify: Should include 9:30 (9:30 + 30min = 10:00 exactly)
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasItem("09:00")))
                .andExpect(jsonPath("$[*]", hasItem("09:15")))
                .andExpect(jsonPath("$[*]", hasItem("09:30"))) // Fits exactly
                .andExpect(jsonPath("$[*]", not(hasItem("09:45")))); // Would end at 10:15
    }

    @Test
    @DisplayName("Should return 404 when service not found")
    void testGetAvailableSlots_ServiceNotFound() throws Exception {
        // Setup availability
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Execute & Verify
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", "99999")) // Non-existent service
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle barber with no availability records")
    void testGetAvailableSlots_NoAvailabilityRecords() throws Exception {
        // Don't create any availability records

        // Execute & Verify
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should handle complex scenario with bookings and breaks")
    void testGetAvailableSlots_ComplexScenario() throws Exception {
        // Setup: Barber works Monday 9:00-17:00
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Add booking at 10:00-10:30
        createBooking(LocalTime.of(10, 0), LocalTime.of(10, 30));

        // Add lunch break 12:00-13:00
        BarberBreak lunchBreak = new BarberBreak();
        lunchBreak.setBarber(testBarber);
        lunchBreak.setLabel("Lunch");
        lunchBreak.setStartTime(LocalTime.of(12, 0));
        lunchBreak.setEndTime(LocalTime.of(13, 0));
        barberBreakRepository.save(lunchBreak);

        // Add another booking at 15:00-15:30
        createBooking(LocalTime.of(15, 0), LocalTime.of(15, 30));

        // Execute & Verify
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasItem("09:00"))) // Morning available
                .andExpect(jsonPath("$[*]", not(hasItem("10:00")))) // Booking conflict
                .andExpect(jsonPath("$[*]", hasItem("10:30"))) // After booking
                .andExpect(jsonPath("$[*]", not(hasItem("12:00")))) // Lunch break
                .andExpect(jsonPath("$[*]", hasItem("13:00"))) // After lunch
                .andExpect(jsonPath("$[*]", not(hasItem("15:00")))) // Booking conflict
                .andExpect(jsonPath("$[*]", hasItem("15:30"))); // Afternoon available
    }

    @Test
    @DisplayName("Should return error when required parameters are missing")
    void testGetAvailableSlots_MissingParameters() throws Exception {
        // Missing barberId - returns 500 due to missing required parameter
        mockMvc.perform(get("/api/availability")
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().is5xxServerError());

        // Missing date
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().is5xxServerError());

        // Missing serviceId
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should handle 15-minute service correctly")
    void testGetAvailableSlots_ShortService() throws Exception {
        // Create a short service (15 minutes)
        ServiceOffered shortService = new ServiceOffered();
        shortService.setName("Beard Trim");
        shortService.setDescription("Quick beard trim");
        shortService.setDurationMinutes(15);
        shortService.setPrice(BigDecimal.valueOf(15.00));
        shortService.setDepositPercentage(50);
        shortService.setActive(true);
        shortService.setCategory(testService.getCategory());
        shortService = serviceRepository.save(shortService);

        // Setup: Barber works Monday 9:00-10:00
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(10, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Execute & Verify: Should include 9:45 (9:45 + 15min = 10:00 exactly)
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", shortService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasItem("09:45"))); // Last possible slot
    }

    @Test
    @DisplayName("Should exclude slots that partially overlap with breaks")
    void testGetAvailableSlots_PartialBreakOverlap() throws Exception {
        // Setup: Barber works Monday 9:00-17:00
        BarberAvailability availability = new BarberAvailability();
        availability.setBarber(testBarber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        barberAvailabilityRepository.save(availability);

        // Break from 12:00-12:30
        BarberBreak shortBreak = new BarberBreak();
        shortBreak.setBarber(testBarber);
        shortBreak.setLabel("Short Break");
        shortBreak.setStartTime(LocalTime.of(12, 0));
        shortBreak.setEndTime(LocalTime.of(12, 30));
        barberBreakRepository.save(shortBreak);

        // Execute & Verify
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasItem("11:30"))) // 11:30-12:00 fits before break
                .andExpect(jsonPath("$[*]", not(hasItem("11:45")))) // 11:45-12:15 overlaps break
                .andExpect(jsonPath("$[*]", not(hasItem("12:00")))) // During break
                .andExpect(jsonPath("$[*]", not(hasItem("12:15")))) // 12:15-12:45 starts in break
                .andExpect(jsonPath("$[*]", hasItem("12:30"))); // After break
    }

    @Test
    @DisplayName("Should handle different days of the week")
    void testGetAvailableSlots_DifferentDaysOfWeek() throws Exception {
        // Setup: Different hours for different days
        // Monday: 9:00-17:00
        BarberAvailability monday = new BarberAvailability();
        monday.setBarber(testBarber);
        monday.setDayOfWeek(DayOfWeek.MONDAY);
        monday.setStartTime(LocalTime.of(9, 0));
        monday.setEndTime(LocalTime.of(17, 0));
        monday.setIsAvailable(true);
        barberAvailabilityRepository.save(monday);

        // Tuesday: 10:00-15:00
        BarberAvailability tuesday = new BarberAvailability();
        tuesday.setBarber(testBarber);
        tuesday.setDayOfWeek(DayOfWeek.TUESDAY);
        tuesday.setStartTime(LocalTime.of(10, 0));
        tuesday.setEndTime(LocalTime.of(15, 0));
        tuesday.setIsAvailable(true);
        barberAvailabilityRepository.save(tuesday);

        LocalDate tuesdayDate = futureDate.plusDays(1);

        // Verify Monday has more slots
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", futureDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", is("09:00")));

        // Verify Tuesday has fewer slots
        mockMvc.perform(get("/api/availability")
                        .param("barberId", testBarber.getId().toString())
                        .param("date", tuesdayDate.toString())
                        .param("serviceId", testService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", is("10:00")))
                .andExpect(jsonPath("$[*]", not(hasItem("09:00")))) // Starts later
                .andExpect(jsonPath("$[*]", not(hasItem("16:00")))); // Ends earlier
    }

    // Helper method to create a booking
    private void createBooking(LocalTime startTime, LocalTime endTime) {
        Booking booking = new Booking();
        booking.setCustomer(testCustomer);
        booking.setBarber(testBarber);
        booking.setService(testService);
        booking.setBookingDate(futureDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.FULLY_PAID);
        booking.setDepositAmount(testService.getPrice());
        booking.setOutstandingBalance(BigDecimal.ZERO);
        booking.setCreatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }
}

