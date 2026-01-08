package com.trim.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trim.booking.config.JwtUtil;
import com.trim.booking.dto.booking.CreateBookingRequest;
import com.trim.booking.entity.*;
import com.trim.booking.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BookingController.createBooking()
 *
 * Tests cover:
 * - Successful booking creation
 * - Conflict detection (double booking prevention)
 * - Validation (missing required fields)
 * - Entity validation (non-existent customer, barber, service)
 * - Business rules (booking in the past)
 * - Time overlap scenarios
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookingControllerCreateBookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceCategoryRepository categoryRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;

    private User customer;
    private User barberUser;
    private Barber barber;
    private ServiceCategory category;
    private ServiceOffered service;
    private String customerToken;

    @BeforeAll
    void setupOnce() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUp() {
        // Clean up before each test
        bookingRepository.deleteAll();
        barberRepository.deleteAll();
        serviceRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create a customer
        customer = new User();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("customer@test.com");
        customer.setPasswordHash("hashedpassword");
        customer.setPhone("+353871234567");
        customer.setRole(User.Role.CUSTOMER);
        customer = userRepository.save(customer);

        // Create a barber user
        barberUser = new User();
        barberUser.setFirstName("Jane");
        barberUser.setLastName("Barber");
        barberUser.setEmail("barber@test.com");
        barberUser.setPasswordHash("hashedpassword");
        barberUser.setPhone("+353877654321");
        barberUser.setRole(User.Role.BARBER);
        barberUser = userRepository.save(barberUser);

        // Create barber entity
        barber = new Barber();
        barber.setUser(barberUser);
        barber.setBio("Expert barber");
        barber.setActive(true);
        barber = barberRepository.save(barber);

        // Create service category
        category = new ServiceCategory("Haircuts");
        category.setActive(true);
        category = categoryRepository.save(category);

        // Create a service
        service = new ServiceOffered();
        service.setName("Standard Haircut");
        service.setDescription("Basic haircut service");
        service.setDurationMinutes(30);
        service.setPrice(new BigDecimal("25.00"));
        service.setDepositPercentage(20);
        service.setActive(true);
        service.setCategory(category);
        service = serviceRepository.save(service);

        // Generate JWT token for customer
        customerToken = jwtUtil.generateToken(customer.getEmail(), "CUSTOMER", customer.getId());
    }

    // ==================== SUCCESSFUL BOOKING TESTS ====================

    @Test
    @DisplayName("Should successfully create a booking with valid data")
    void createBooking_WithValidData_ReturnsCreated() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setPaymentMethod("pay_online");

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customer.id").value(customer.getId()))
                .andExpect(jsonPath("$.barber.id").value(barber.getId()))
                .andExpect(jsonPath("$.service.id").value(service.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paymentStatus").value("DEPOSIT_PENDING"))
                .andExpect(jsonPath("$.startTime").value("10:00:00"))
                .andExpect(jsonPath("$.endTime").value("10:30:00")); // 30-minute service
    }

    @Test
    @DisplayName("Should create booking with default payment method when not specified")
    void createBooking_WithoutPaymentMethod_UsesDefaultPayOnline() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(14, 0));
        // paymentMethod not set

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Should calculate correct end time based on service duration")
    void createBooking_CalculatesCorrectEndTime() throws Exception {
        // Create a 60-minute service
        ServiceOffered longService = new ServiceOffered();
        longService.setName("Premium Haircut");
        longService.setDescription("Full service haircut");
        longService.setDurationMinutes(60);
        longService.setPrice(new BigDecimal("50.00"));
        longService.setDepositPercentage(25);
        longService.setActive(true);
        longService.setCategory(category);
        longService = serviceRepository.save(longService);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(longService.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(11, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("11:00:00"))
                .andExpect(jsonPath("$.endTime").value("12:00:00")); // 60-minute service
    }

    // ==================== CONFLICT DETECTION TESTS ====================

    @Test
    @DisplayName("Should reject booking when exact time slot is already taken")
    void createBooking_WhenExactSlotTaken_ReturnsConflict() throws Exception {
        // First, create an existing booking
        CreateBookingRequest firstRequest = new CreateBookingRequest();
        firstRequest.setCustomerId(customer.getId());
        firstRequest.setBarberId(barber.getId());
        firstRequest.setServiceId(service.getId());
        firstRequest.setBookingDate(LocalDate.now().plusDays(1));
        firstRequest.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Try to book the same slot
        CreateBookingRequest conflictRequest = new CreateBookingRequest();
        conflictRequest.setCustomerId(customer.getId());
        conflictRequest.setBarberId(barber.getId());
        conflictRequest.setServiceId(service.getId());
        conflictRequest.setBookingDate(LocalDate.now().plusDays(1));
        conflictRequest.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("no longer available")));
    }

    @Test
    @DisplayName("Should reject booking when new booking overlaps start of existing booking")
    void createBooking_WhenOverlapsStartOfExisting_ReturnsConflict() throws Exception {
        // Create booking from 10:00 to 10:30
        CreateBookingRequest existingRequest = new CreateBookingRequest();
        existingRequest.setCustomerId(customer.getId());
        existingRequest.setBarberId(barber.getId());
        existingRequest.setServiceId(service.getId());
        existingRequest.setBookingDate(LocalDate.now().plusDays(1));
        existingRequest.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingRequest)))
                .andExpect(status().isCreated());

        // Try to book from 9:45 to 10:15 (overlaps start)
        CreateBookingRequest conflictRequest = new CreateBookingRequest();
        conflictRequest.setCustomerId(customer.getId());
        conflictRequest.setBarberId(barber.getId());
        conflictRequest.setServiceId(service.getId());
        conflictRequest.setBookingDate(LocalDate.now().plusDays(1));
        conflictRequest.setStartTime(LocalTime.of(9, 45));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should reject booking when new booking overlaps end of existing booking")
    void createBooking_WhenOverlapsEndOfExisting_ReturnsConflict() throws Exception {
        // Create booking from 10:00 to 10:30
        CreateBookingRequest existingRequest = new CreateBookingRequest();
        existingRequest.setCustomerId(customer.getId());
        existingRequest.setBarberId(barber.getId());
        existingRequest.setServiceId(service.getId());
        existingRequest.setBookingDate(LocalDate.now().plusDays(1));
        existingRequest.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingRequest)))
                .andExpect(status().isCreated());

        // Try to book from 10:15 to 10:45 (overlaps end)
        CreateBookingRequest conflictRequest = new CreateBookingRequest();
        conflictRequest.setCustomerId(customer.getId());
        conflictRequest.setBarberId(barber.getId());
        conflictRequest.setServiceId(service.getId());
        conflictRequest.setBookingDate(LocalDate.now().plusDays(1));
        conflictRequest.setStartTime(LocalTime.of(10, 15));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should reject booking when new booking is completely inside existing booking")
    void createBooking_WhenInsideExisting_ReturnsConflict() throws Exception {
        // Create 60-minute booking from 10:00 to 11:00
        ServiceOffered longService = new ServiceOffered();
        longService.setName("Long Service");
        longService.setDurationMinutes(60);
        longService.setPrice(new BigDecimal("50.00"));
        longService.setDepositPercentage(20);
        longService.setActive(true);
        longService.setCategory(category);
        longService = serviceRepository.save(longService);

        CreateBookingRequest existingRequest = new CreateBookingRequest();
        existingRequest.setCustomerId(customer.getId());
        existingRequest.setBarberId(barber.getId());
        existingRequest.setServiceId(longService.getId());
        existingRequest.setBookingDate(LocalDate.now().plusDays(1));
        existingRequest.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingRequest)))
                .andExpect(status().isCreated());

        // Try to book 30-minute slot from 10:15 to 10:45 (inside existing)
        CreateBookingRequest conflictRequest = new CreateBookingRequest();
        conflictRequest.setCustomerId(customer.getId());
        conflictRequest.setBarberId(barber.getId());
        conflictRequest.setServiceId(service.getId()); // 30-minute service
        conflictRequest.setBookingDate(LocalDate.now().plusDays(1));
        conflictRequest.setStartTime(LocalTime.of(10, 15));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should allow booking immediately after existing booking ends")
    void createBooking_WhenImmediatelyAfterExisting_Succeeds() throws Exception {
        // Create booking from 10:00 to 10:30
        CreateBookingRequest existingRequest = new CreateBookingRequest();
        existingRequest.setCustomerId(customer.getId());
        existingRequest.setBarberId(barber.getId());
        existingRequest.setServiceId(service.getId());
        existingRequest.setBookingDate(LocalDate.now().plusDays(1));
        existingRequest.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingRequest)))
                .andExpect(status().isCreated());

        // Book from 10:30 to 11:00 (immediately after)
        CreateBookingRequest nextRequest = new CreateBookingRequest();
        nextRequest.setCustomerId(customer.getId());
        nextRequest.setBarberId(barber.getId());
        nextRequest.setServiceId(service.getId());
        nextRequest.setBookingDate(LocalDate.now().plusDays(1));
        nextRequest.setStartTime(LocalTime.of(10, 30));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nextRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should allow booking immediately before existing booking starts")
    void createBooking_WhenImmediatelyBeforeExisting_Succeeds() throws Exception {
        // Create booking from 10:30 to 11:00
        CreateBookingRequest existingRequest = new CreateBookingRequest();
        existingRequest.setCustomerId(customer.getId());
        existingRequest.setBarberId(barber.getId());
        existingRequest.setServiceId(service.getId());
        existingRequest.setBookingDate(LocalDate.now().plusDays(1));
        existingRequest.setStartTime(LocalTime.of(10, 30));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingRequest)))
                .andExpect(status().isCreated());

        // Book from 10:00 to 10:30 (immediately before)
        CreateBookingRequest beforeRequest = new CreateBookingRequest();
        beforeRequest.setCustomerId(customer.getId());
        beforeRequest.setBarberId(barber.getId());
        beforeRequest.setServiceId(service.getId());
        beforeRequest.setBookingDate(LocalDate.now().plusDays(1));
        beforeRequest.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beforeRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should allow same time slot with different barber")
    void createBooking_SameTimeSlotDifferentBarber_Succeeds() throws Exception {
        // Create second barber
        User secondBarberUser = new User();
        secondBarberUser.setFirstName("Bob");
        secondBarberUser.setLastName("Barber");
        secondBarberUser.setEmail("barber2@test.com");
        secondBarberUser.setPasswordHash("hashedpassword");
        secondBarberUser.setPhone("+353871112222");
        secondBarberUser.setRole(User.Role.BARBER);
        secondBarberUser = userRepository.save(secondBarberUser);

        Barber secondBarber = new Barber();
        secondBarber.setUser(secondBarberUser);
        secondBarber.setActive(true);
        secondBarber = barberRepository.save(secondBarber);

        // Create booking with first barber
        CreateBookingRequest firstRequest = new CreateBookingRequest();
        firstRequest.setCustomerId(customer.getId());
        firstRequest.setBarberId(barber.getId());
        firstRequest.setServiceId(service.getId());
        firstRequest.setBookingDate(LocalDate.now().plusDays(1));
        firstRequest.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Create same time slot booking with second barber
        CreateBookingRequest secondRequest = new CreateBookingRequest();
        secondRequest.setCustomerId(customer.getId());
        secondRequest.setBarberId(secondBarber.getId());
        secondRequest.setServiceId(service.getId());
        secondRequest.setBookingDate(LocalDate.now().plusDays(1));
        secondRequest.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should allow same time slot on different date")
    void createBooking_SameTimeSlotDifferentDate_Succeeds() throws Exception {
        // Create booking for tomorrow
        CreateBookingRequest firstRequest = new CreateBookingRequest();
        firstRequest.setCustomerId(customer.getId());
        firstRequest.setBarberId(barber.getId());
        firstRequest.setServiceId(service.getId());
        firstRequest.setBookingDate(LocalDate.now().plusDays(1));
        firstRequest.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Create same time slot booking for day after tomorrow
        CreateBookingRequest secondRequest = new CreateBookingRequest();
        secondRequest.setCustomerId(customer.getId());
        secondRequest.setBarberId(barber.getId());
        secondRequest.setServiceId(service.getId());
        secondRequest.setBookingDate(LocalDate.now().plusDays(2));
        secondRequest.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated());
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("Should reject booking when customer ID is missing")
    void createBooking_MissingCustomerId_ReturnsBadRequest() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        // customerId not set
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject booking when barber ID is missing")
    void createBooking_MissingBarberId_ReturnsBadRequest() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        // barberId not set
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject booking when service ID is missing")
    void createBooking_MissingServiceId_ReturnsBadRequest() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        // serviceId not set
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject booking when booking date is missing")
    void createBooking_MissingBookingDate_ReturnsBadRequest() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        // bookingDate not set
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject booking when start time is missing")
    void createBooking_MissingStartTime_ReturnsBadRequest() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        // startTime not set

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== ENTITY VALIDATION TESTS ====================

    @Test
    @DisplayName("Should reject booking when customer does not exist")
    void createBooking_NonExistentCustomer_ReturnsNotFound() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(99999L); // Non-existent customer
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Customer not found")));
    }

    @Test
    @DisplayName("Should reject booking when barber does not exist")
    void createBooking_NonExistentBarber_ReturnsNotFound() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(99999L); // Non-existent barber
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Barber not found")));
    }

    @Test
    @DisplayName("Should reject booking when service does not exist")
    void createBooking_NonExistentService_ReturnsNotFound() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(99999L); // Non-existent service
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Service not found")));
    }

    // ==================== BUSINESS RULE TESTS ====================

    @Test
    @DisplayName("Should reject booking when date is in the past")
    void createBooking_PastDate_ReturnsBadRequest() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().minusDays(1)); // Yesterday
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("past")));
    }

    @Test
    @DisplayName("Should set booking status to PENDING initially")
    void createBooking_InitialStatus_IsPending() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should set payment status to DEPOSIT_PENDING initially")
    void createBooking_InitialPaymentStatus_IsDepositPending() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentStatus").value("DEPOSIT_PENDING"));
    }

    @Test
    @DisplayName("Should set outstanding balance to service price initially")
    void createBooking_OutstandingBalance_EqualsServicePrice() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.outstandingBalance").value(25.00)); // Service price
    }

    @Test
    @DisplayName("Should set expiry time for pending booking")
    void createBooking_SetsExpiryTime() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    // ==================== CANCELLED BOOKING TESTS ====================

    @Test
    @DisplayName("Should allow booking same slot if previous booking was cancelled")
    void createBooking_WhenPreviousBookingCancelled_Succeeds() throws Exception {
        // Create first booking
        CreateBookingRequest firstRequest = new CreateBookingRequest();
        firstRequest.setCustomerId(customer.getId());
        firstRequest.setBarberId(barber.getId());
        firstRequest.setServiceId(service.getId());
        firstRequest.setBookingDate(LocalDate.now().plusDays(1));
        firstRequest.setStartTime(LocalTime.of(10, 0));

        MvcResult result = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        // Get the booking ID and cancel it
        String responseBody = result.getResponse().getContentAsString();
        Long bookingId = objectMapper.readTree(responseBody).get("id").asLong();

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Now try to book the same slot again
        CreateBookingRequest secondRequest = new CreateBookingRequest();
        secondRequest.setCustomerId(customer.getId());
        secondRequest.setBarberId(barber.getId());
        secondRequest.setServiceId(service.getId());
        secondRequest.setBookingDate(LocalDate.now().plusDays(1));
        secondRequest.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle booking at end of day")
    void createBooking_AtEndOfDay_Succeeds() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(23, 30)); // 11:30 PM, ends at midnight

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("23:30:00"))
                .andExpect(jsonPath("$.endTime").value("00:00:00"));
    }

    @Test
    @DisplayName("Should handle booking at start of day")
    void createBooking_AtStartOfDay_Succeeds() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(0, 0)); // Midnight

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("00:00:00"))
                .andExpect(jsonPath("$.endTime").value("00:30:00"));
    }

    // ==================== BLACKLISTED CUSTOMER TESTS ====================

    @Test
    @DisplayName("Should return 403 when blacklisted customer tries to create booking")
    void createBooking_BlacklistedCustomer_ReturnsForbidden() throws Exception {
        // Blacklist the customer
        customer.setBlacklisted(true);
        customer.setBlacklistReason("Repeated no-shows");
        customer.setBlacklistedAt(java.time.LocalDateTime.now());
        userRepository.save(customer);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setPaymentMethod("pay_online");

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Customer is blacklisted")))
                .andExpect(jsonPath("$.message", containsString("Repeated no-shows")));
    }

    @Test
    @DisplayName("Should include blacklist reason in error message")
    void createBooking_BlacklistedCustomer_IncludesReasonInError() throws Exception {
        // Blacklist with specific reason
        customer.setBlacklisted(true);
        customer.setBlacklistReason("Abusive behavior towards staff");
        customer.setBlacklistedAt(java.time.LocalDateTime.now());
        userRepository.save(customer);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Customer is blacklisted: Abusive behavior towards staff")));
    }

    @Test
    @DisplayName("Should allow booking after customer is unblacklisted")
    void createBooking_AfterUnblacklist_Succeeds() throws Exception {
        // First blacklist the customer
        customer.setBlacklisted(true);
        customer.setBlacklistReason("Previous offense");
        customer.setBlacklistedAt(java.time.LocalDateTime.now());
        userRepository.save(customer);

        // Then unblacklist them
        customer.setBlacklisted(false);
        customer.setBlacklistReason(null);
        customer.setBlacklistedAt(null);
        userRepository.save(customer);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setCustomerId(customer.getId());
        request.setBarberId(barber.getId());
        request.setServiceId(service.getId());
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setPaymentMethod("pay_online");

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }
}

