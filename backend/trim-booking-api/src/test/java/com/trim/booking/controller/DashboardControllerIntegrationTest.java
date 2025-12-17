package com.trim.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DashboardController
 *
 * Tests cover:
 * - Get admin dashboard stats (admin only)
 * - Authorization checks
 * - Stats accuracy with data
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DashboardControllerIntegrationTest {

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

    private User admin;
    private User customer;
    private User barberUser;
    private Barber barber;
    private ServiceCategory category;
    private ServiceOffered service;
    private String adminToken;
    private String customerToken;
    private String barberToken;

    @BeforeAll
    void setupOnce() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUp() {
        // Clean up before each test
        bookingRepository.deleteAll();
        serviceRepository.deleteAll();
        categoryRepository.deleteAll();
        barberRepository.deleteAll();
        userRepository.deleteAll();

        // Create an admin user
        admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@test.com");
        admin.setPasswordHash("hashedpassword");
        admin.setPhone("+353871111111");
        admin.setRole(User.Role.ADMIN);
        admin = userRepository.save(admin);

        // Create a customer
        customer = new User();
        customer.setFirstName("John");
        customer.setLastName("Customer");
        customer.setEmail("customer@test.com");
        customer.setPasswordHash("hashedpassword");
        customer.setPhone("+353872222222");
        customer.setRole(User.Role.CUSTOMER);
        customer = userRepository.save(customer);

        // Create a barber user
        barberUser = new User();
        barberUser.setFirstName("Jane");
        barberUser.setLastName("Barber");
        barberUser.setEmail("barber@test.com");
        barberUser.setPasswordHash("hashedpassword");
        barberUser.setPhone("+353873333333");
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

        // Generate JWT tokens
        adminToken = jwtUtil.generateToken(admin.getEmail(), "ADMIN", admin.getId());
        customerToken = jwtUtil.generateToken(customer.getEmail(), "CUSTOMER", customer.getId());
        barberToken = jwtUtil.generateToken(barberUser.getEmail(), "BARBER", barberUser.getId());
    }

    // ==================== GET ADMIN DASHBOARD TESTS ====================

    @Test
    @DisplayName("Should return dashboard stats when admin")
    void getAdminDashboard_AsAdmin_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/dashboard/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings").exists())
                .andExpect(jsonPath("$.todaysBookings").exists())
                .andExpect(jsonPath("$.upcomingBookings").exists())
                .andExpect(jsonPath("$.totalRevenue").exists())
                .andExpect(jsonPath("$.thisMonthRevenue").exists())
                .andExpect(jsonPath("$.activeCustomers").exists())
                .andExpect(jsonPath("$.activeBarbers").exists())
                .andExpect(jsonPath("$.popularServices").exists())
                .andExpect(jsonPath("$.recentBookings").exists());
    }

    @Test
    @DisplayName("Should reject dashboard access when customer")
    void getAdminDashboard_AsCustomer_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard/admin")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject dashboard access when barber")
    void getAdminDashboard_AsBarber_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard/admin")
                        .header("Authorization", "Bearer " + barberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject dashboard access without authentication")
    void getAdminDashboard_WithoutAuth_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return correct stats with bookings data")
    void getAdminDashboard_WithBookings_ReturnsCorrectStats() throws Exception {
        // Create a booking for today
        Booking todayBooking = new Booking();
        todayBooking.setCustomer(customer);
        todayBooking.setBarber(barber);
        todayBooking.setService(service);
        todayBooking.setBookingDate(LocalDate.now());
        todayBooking.setStartTime(LocalTime.of(10, 0));
        todayBooking.setEndTime(LocalTime.of(10, 30));
        todayBooking.setStatus(Booking.BookingStatus.CONFIRMED);
        todayBooking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        todayBooking.setDepositAmount(new BigDecimal("5.00"));
        todayBooking.setOutstandingBalance(new BigDecimal("20.00"));
        bookingRepository.save(todayBooking);

        // Create an upcoming booking
        Booking upcomingBooking = new Booking();
        upcomingBooking.setCustomer(customer);
        upcomingBooking.setBarber(barber);
        upcomingBooking.setService(service);
        upcomingBooking.setBookingDate(LocalDate.now().plusDays(3));
        upcomingBooking.setStartTime(LocalTime.of(14, 0));
        upcomingBooking.setEndTime(LocalTime.of(14, 30));
        upcomingBooking.setStatus(Booking.BookingStatus.CONFIRMED);
        upcomingBooking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        upcomingBooking.setDepositAmount(new BigDecimal("5.00"));
        upcomingBooking.setOutstandingBalance(new BigDecimal("20.00"));
        bookingRepository.save(upcomingBooking);

        mockMvc.perform(get("/api/dashboard/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.todaysBookings").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.activeBarbers").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Should return zero stats when no data")
    void getAdminDashboard_NoData_ReturnsZeroStats() throws Exception {
        // Clean all bookings
        bookingRepository.deleteAll();

        mockMvc.perform(get("/api/dashboard/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings").value(0))
                .andExpect(jsonPath("$.todaysBookings").value(0));
    }
}

