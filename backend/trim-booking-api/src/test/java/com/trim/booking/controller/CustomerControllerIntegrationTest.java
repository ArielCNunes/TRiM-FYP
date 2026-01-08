package com.trim.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trim.booking.config.JwtUtil;
import com.trim.booking.dto.customer.BlacklistRequest;
import com.trim.booking.entity.Booking;
import com.trim.booking.entity.Barber;
import com.trim.booking.entity.ServiceCategory;
import com.trim.booking.entity.ServiceOffered;
import com.trim.booking.entity.User;
import com.trim.booking.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CustomerController
 *
 * Tests cover:
 * - Get all customers (paginated)
 * - Get single customer by ID
 * - Blacklist customer
 * - Unblacklist customer
 * - Authorization checks (admin only)
 * - Validation errors
 * - No-show count tracking
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;

    private User admin;
    private User customer;
    private User customer2;
    private User barberUser;
    private Barber barber;
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
        serviceCategoryRepository.deleteAll();
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

        // Create another customer
        customer2 = new User();
        customer2.setFirstName("Jane");
        customer2.setLastName("Doe");
        customer2.setEmail("jane@test.com");
        customer2.setPasswordHash("hashedpassword");
        customer2.setPhone("+353872223333");
        customer2.setRole(User.Role.CUSTOMER);
        customer2 = userRepository.save(customer2);

        // Create a barber user
        barberUser = new User();
        barberUser.setFirstName("Bob");
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

        // Create service category and service for bookings
        ServiceCategory category = new ServiceCategory("Haircuts");
        category.setActive(true);
        category = serviceCategoryRepository.save(category);

        service = new ServiceOffered();
        service.setName("Standard Haircut");
        service.setDescription("Basic haircut");
        service.setDurationMinutes(30);
        service.setPrice(new BigDecimal("25.00"));
        service.setDepositPercentage(20);
        service.setCategory(category);
        service.setActive(true);
        service = serviceRepository.save(service);

        // Generate JWT tokens
        adminToken = jwtUtil.generateToken(admin.getEmail(), "ADMIN", admin.getId());
        customerToken = jwtUtil.generateToken(customer.getEmail(), "CUSTOMER", customer.getId());
        barberToken = jwtUtil.generateToken(barberUser.getEmail(), "BARBER", barberUser.getId());
    }

    // ==================== GET ALL CUSTOMERS TESTS ====================

    @Test
    @DisplayName("Should return paginated list of customers for admin")
    void getCustomers_AsAdmin_ReturnsPaginatedList() throws Exception {
        mockMvc.perform(get("/api/admin/customers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customers", hasSize(2)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.customers[*].email", containsInAnyOrder("customer@test.com", "jane@test.com")));
    }

    @Test
    @DisplayName("Should return paginated customers with custom page size")
    void getCustomers_WithCustomPageSize_ReturnsPaginatedList() throws Exception {
        mockMvc.perform(get("/api/admin/customers")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customers", hasSize(1)))
                .andExpect(jsonPath("$.size", is(1)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    @DisplayName("Should return 403 when customer tries to access customer list")
    void getCustomers_AsCustomer_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/customers")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 when barber tries to access customer list")
    void getCustomers_AsBarber_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/customers")
                        .header("Authorization", "Bearer " + barberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 when no authentication provided")
    void getCustomers_NoAuth_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/customers"))
                .andExpect(status().isForbidden());
    }

    // ==================== GET SINGLE CUSTOMER TESTS ====================

    @Test
    @DisplayName("Should return customer details for admin")
    void getCustomer_AsAdmin_ReturnsCustomerDetails() throws Exception {
        mockMvc.perform(get("/api/admin/customers/{id}", customer.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customer.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Customer")))
                .andExpect(jsonPath("$.email", is("customer@test.com")))
                .andExpect(jsonPath("$.blacklisted", is(false)))
                .andExpect(jsonPath("$.blacklistReason", nullValue()))
                .andExpect(jsonPath("$.noShowCount", is(0)));
    }

    @Test
    @DisplayName("Should return 404 when customer not found")
    void getCustomer_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/admin/customers/{id}", 99999L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when trying to get non-customer user")
    void getCustomer_NonCustomerUser_Returns404() throws Exception {
        // Trying to get the admin user (who is not a CUSTOMER role)
        mockMvc.perform(get("/api/admin/customers/{id}", admin.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should include no-show count in customer details")
    void getCustomer_WithNoShows_ReturnsCorrectCount() throws Exception {
        // Create some no-show bookings for the customer
        createBookingWithStatus(customer, Booking.BookingStatus.NO_SHOW);
        createBookingWithStatus(customer, Booking.BookingStatus.NO_SHOW);
        createBookingWithStatus(customer, Booking.BookingStatus.COMPLETED);

        mockMvc.perform(get("/api/admin/customers/{id}", customer.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noShowCount", is(2)));
    }

    // ==================== BLACKLIST CUSTOMER TESTS ====================

    @Test
    @DisplayName("Should blacklist customer successfully")
    void blacklistCustomer_AsAdmin_Success() throws Exception {
        BlacklistRequest request = new BlacklistRequest("Repeated no-shows");

        mockMvc.perform(put("/api/admin/customers/{id}/blacklist", customer.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customer.getId().intValue())))
                .andExpect(jsonPath("$.blacklisted", is(true)))
                .andExpect(jsonPath("$.blacklistReason", is("Repeated no-shows")))
                .andExpect(jsonPath("$.blacklistedAt", notNullValue()));

        // Verify in database
        User updatedCustomer = userRepository.findById(customer.getId()).orElseThrow();
        assertTrue(updatedCustomer.getBlacklisted());
        assertEquals("Repeated no-shows", updatedCustomer.getBlacklistReason());
        assertNotNull(updatedCustomer.getBlacklistedAt());
    }

    @Test
    @DisplayName("Should return 400 when blacklist reason is empty")
    void blacklistCustomer_EmptyReason_ReturnsBadRequest() throws Exception {
        BlacklistRequest request = new BlacklistRequest("");

        mockMvc.perform(put("/api/admin/customers/{id}/blacklist", customer.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when blacklist reason exceeds 500 characters")
    void blacklistCustomer_ReasonTooLong_ReturnsBadRequest() throws Exception {
        String longReason = "a".repeat(501);
        BlacklistRequest request = new BlacklistRequest(longReason);

        mockMvc.perform(put("/api/admin/customers/{id}/blacklist", customer.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 403 when customer tries to blacklist")
    void blacklistCustomer_AsCustomer_ReturnsForbidden() throws Exception {
        BlacklistRequest request = new BlacklistRequest("Some reason");

        mockMvc.perform(put("/api/admin/customers/{id}/blacklist", customer.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when blacklisting non-existent customer")
    void blacklistCustomer_NotFound_Returns404() throws Exception {
        BlacklistRequest request = new BlacklistRequest("Some reason");

        mockMvc.perform(put("/api/admin/customers/{id}/blacklist", 99999L)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ==================== UNBLACKLIST CUSTOMER TESTS ====================

    @Test
    @DisplayName("Should unblacklist customer successfully")
    void unblacklistCustomer_AsAdmin_Success() throws Exception {
        // First blacklist the customer
        customer.setBlacklisted(true);
        customer.setBlacklistReason("Previous offense");
        customer.setBlacklistedAt(java.time.LocalDateTime.now());
        userRepository.save(customer);

        mockMvc.perform(put("/api/admin/customers/{id}/unblacklist", customer.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customer.getId().intValue())))
                .andExpect(jsonPath("$.blacklisted", is(false)))
                .andExpect(jsonPath("$.blacklistReason", nullValue()))
                .andExpect(jsonPath("$.blacklistedAt", nullValue()));

        // Verify in database
        User updatedCustomer = userRepository.findById(customer.getId()).orElseThrow();
        assertFalse(updatedCustomer.getBlacklisted());
        assertNull(updatedCustomer.getBlacklistReason());
        assertNull(updatedCustomer.getBlacklistedAt());
    }

    @Test
    @DisplayName("Should return 403 when customer tries to unblacklist")
    void unblacklistCustomer_AsCustomer_ReturnsForbidden() throws Exception {
        mockMvc.perform(put("/api/admin/customers/{id}/unblacklist", customer.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when unblacklisting non-existent customer")
    void unblacklistCustomer_NotFound_Returns404() throws Exception {
        mockMvc.perform(put("/api/admin/customers/{id}/unblacklist", 99999L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ==================== HELPER METHODS ====================

    private Booking createBookingWithStatus(User bookingCustomer, Booking.BookingStatus status) {
        Booking booking = new Booking();
        booking.setCustomer(bookingCustomer);
        booking.setBarber(barber);
        booking.setService(service);
        booking.setBookingDate(LocalDate.now().plusDays(1));
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(10, 30));
        booking.setStatus(status);
        booking.setPaymentStatus(Booking.PaymentStatus.DEPOSIT_PAID);
        booking.setDepositAmount(new BigDecimal("5.00"));
        booking.setOutstandingBalance(new BigDecimal("20.00"));
        return bookingRepository.save(booking);
    }
}

