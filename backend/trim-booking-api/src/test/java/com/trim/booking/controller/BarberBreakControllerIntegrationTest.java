package com.trim.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trim.booking.config.JwtUtil;
import com.trim.booking.dto.barber.CreateBarberBreakRequest;
import com.trim.booking.dto.barber.UpdateBarberBreakRequest;
import com.trim.booking.entity.Barber;
import com.trim.booking.entity.BarberBreak;
import com.trim.booking.entity.Business;
import com.trim.booking.entity.User;
import com.trim.booking.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BarberBreakController
 *
 * Tests cover:
 * - Create break (admin/barber only)
 * - Update break (admin/barber only)
 * - Get breaks by barber ID (admin/barber only)
 * - Delete break (admin/barber only)
 * - Authorization checks
 * - Validation errors
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BarberBreakControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private BarberBreakRepository breakRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BarberAvailabilityRepository barberAvailabilityRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;
    private Business business;

    private User admin;
    private User customer;
    private User barberUser;
    private Barber barber;
    private BarberBreak barberBreak;
    private String adminToken;
    private String customerToken;
    private String barberToken;

    @BeforeAll
    void setupOnce() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create a business (persists across all tests in this class)
        business = new Business();
        business.setName("Test Barbershop Breaks");
        business = businessRepository.save(business);
    }

    @BeforeEach
    void setUp() {
        // Comprehensive cleanup before each test (business persists from @BeforeAll)
        bookingRepository.deleteAll();
        barberAvailabilityRepository.deleteAll();
        breakRepository.deleteAll();
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
        admin.setBusiness(business);
        admin = userRepository.save(admin);

        // Create a customer
        customer = new User();
        customer.setFirstName("John");
        customer.setLastName("Customer");
        customer.setEmail("customer@test.com");
        customer.setPasswordHash("hashedpassword");
        customer.setPhone("+353872222222");
        customer.setRole(User.Role.CUSTOMER);
        customer.setBusiness(business);
        customer = userRepository.save(customer);

        // Create a barber user
        barberUser = new User();
        barberUser.setFirstName("Jane");
        barberUser.setLastName("Barber");
        barberUser.setEmail("barber@test.com");
        barberUser.setPasswordHash("hashedpassword");
        barberUser.setPhone("+353873333333");
        barberUser.setRole(User.Role.BARBER);
        barberUser.setBusiness(business);
        barberUser = userRepository.save(barberUser);

        // Create barber entity
        barber = new Barber();
        barber.setUser(barberUser);
        barber.setBio("Expert barber");
        barber.setActive(true);
        barber.setBusiness(business);
        barber = barberRepository.save(barber);

        // Create a break
        barberBreak = new BarberBreak();
        barberBreak.setBarber(barber);
        barberBreak.setStartTime(LocalTime.of(12, 0));
        barberBreak.setEndTime(LocalTime.of(13, 0));
        barberBreak.setLabel("Lunch Break");
        barberBreak.setBusiness(business);
        barberBreak = breakRepository.save(barberBreak);

        // Generate JWT tokens with businessId
        adminToken = jwtUtil.generateToken(admin.getEmail(), "ADMIN", admin.getId(), business.getId());
        customerToken = jwtUtil.generateToken(customer.getEmail(), "CUSTOMER", customer.getId(), business.getId());
        barberToken = jwtUtil.generateToken(barberUser.getEmail(), "BARBER", barberUser.getId(), business.getId());
    }

    // ==================== CREATE BREAK TESTS ====================

    @Test
    @DisplayName("Should successfully create a break when admin")
    void createBreak_AsAdmin_ReturnsCreated() throws Exception {
        CreateBarberBreakRequest request = new CreateBarberBreakRequest();
        request.setBarberId(barber.getId());
        request.setStartTime("14:00");
        request.setEndTime("14:30");
        request.setLabel("Afternoon Break");

        mockMvc.perform(post("/api/barber-breaks")
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.startTime").value("14:00:00"))
                .andExpect(jsonPath("$.endTime").value("14:30:00"))
                .andExpect(jsonPath("$.label").value("Afternoon Break"));
    }

    @Test
    @DisplayName("Should successfully create a break when barber")
    void createBreak_AsBarber_ReturnsCreated() throws Exception {
        CreateBarberBreakRequest request = new CreateBarberBreakRequest();
        request.setBarberId(barber.getId());
        request.setStartTime("15:00");
        request.setEndTime("15:15");
        request.setLabel("Coffee Break");

        mockMvc.perform(post("/api/barber-breaks")
                        .header("Authorization", "Bearer " + barberToken)
                        .header("X-Business-Slug", business.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Coffee Break"));
    }

    @Test
    @DisplayName("Should reject create break when customer")
    void createBreak_AsCustomer_ReturnsForbidden() throws Exception {
        CreateBarberBreakRequest request = new CreateBarberBreakRequest();
        request.setBarberId(barber.getId());
        request.setStartTime("14:00");
        request.setEndTime("14:30");

        mockMvc.perform(post("/api/barber-breaks")
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject create break without authentication")
    void createBreak_WithoutAuth_ReturnsForbidden() throws Exception {
        CreateBarberBreakRequest request = new CreateBarberBreakRequest();
        request.setBarberId(barber.getId());
        request.setStartTime("14:00");
        request.setEndTime("14:30");

        mockMvc.perform(post("/api/barber-breaks")
                        .header("X-Business-Slug", business.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject create break with missing required fields")
    void createBreak_WithMissingFields_ReturnsBadRequest() throws Exception {
        CreateBarberBreakRequest request = new CreateBarberBreakRequest();
        // Missing all required fields

        mockMvc.perform(post("/api/barber-breaks")
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should successfully create a day-specific break")
    void createBreak_WithDayOfWeek_ReturnsCreated() throws Exception {
        CreateBarberBreakRequest request = new CreateBarberBreakRequest();
        request.setBarberId(barber.getId());
        request.setStartTime("14:00");
        request.setEndTime("14:30");
        request.setLabel("Friday Break");
        request.setDayOfWeek("FRIDAY");

        mockMvc.perform(post("/api/barber-breaks")
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dayOfWeek").value("FRIDAY"))
                .andExpect(jsonPath("$.label").value("Friday Break"));
    }

    @Test
    @DisplayName("Should create break with null dayOfWeek for all days")
    void createBreak_WithoutDayOfWeek_ReturnsCreatedWithNullDay() throws Exception {
        CreateBarberBreakRequest request = new CreateBarberBreakRequest();
        request.setBarberId(barber.getId());
        request.setStartTime("16:00");
        request.setEndTime("16:15");
        request.setLabel("Daily Break");

        mockMvc.perform(post("/api/barber-breaks")
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dayOfWeek").doesNotExist())
                .andExpect(jsonPath("$.label").value("Daily Break"));
    }

    @Test
    @DisplayName("Should return not found when creating break for non-existent barber")
    void createBreak_NonExistentBarber_ReturnsNotFound() throws Exception {
        CreateBarberBreakRequest request = new CreateBarberBreakRequest();
        request.setBarberId(99999L);
        request.setStartTime("14:00");
        request.setEndTime("14:30");

        mockMvc.perform(post("/api/barber-breaks")
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ==================== UPDATE BREAK TESTS ====================

    @Test
    @DisplayName("Should successfully update break when admin")
    void updateBreak_AsAdmin_ReturnsOk() throws Exception {
        UpdateBarberBreakRequest request = new UpdateBarberBreakRequest();
        request.setStartTime("12:30");
        request.setEndTime("13:30");
        request.setLabel("Extended Lunch");

        mockMvc.perform(put("/api/barber-breaks/" + barberBreak.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("12:30:00"))
                .andExpect(jsonPath("$.endTime").value("13:30:00"))
                .andExpect(jsonPath("$.label").value("Extended Lunch"));
    }

    @Test
    @DisplayName("Should successfully update break when barber")
    void updateBreak_AsBarber_ReturnsOk() throws Exception {
        UpdateBarberBreakRequest request = new UpdateBarberBreakRequest();
        request.setStartTime("11:30");
        request.setEndTime("12:30");
        request.setLabel("Early Lunch");

        mockMvc.perform(put("/api/barber-breaks/" + barberBreak.getId())
                        .header("Authorization", "Bearer " + barberToken)
                        .header("X-Business-Slug", business.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Early Lunch"));
    }

    @Test
    @DisplayName("Should reject update break when customer")
    void updateBreak_AsCustomer_ReturnsForbidden() throws Exception {
        UpdateBarberBreakRequest request = new UpdateBarberBreakRequest();
        request.setStartTime("12:30");
        request.setEndTime("13:30");

        mockMvc.perform(put("/api/barber-breaks/" + barberBreak.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should successfully update break with day of week")
    void updateBreak_WithDayOfWeek_ReturnsOk() throws Exception {
        UpdateBarberBreakRequest request = new UpdateBarberBreakRequest();
        request.setStartTime("12:00");
        request.setEndTime("13:00");
        request.setLabel("Monday Lunch");
        request.setDayOfWeek("MONDAY");

        mockMvc.perform(put("/api/barber-breaks/" + barberBreak.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.label").value("Monday Lunch"));
    }

    @Test
    @DisplayName("Should return not found when updating non-existent break")
    void updateBreak_NonExistentId_ReturnsNotFound() throws Exception {
        UpdateBarberBreakRequest request = new UpdateBarberBreakRequest();
        request.setStartTime("12:30");
        request.setEndTime("13:30");

        mockMvc.perform(put("/api/barber-breaks/99999")
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ==================== GET BREAKS BY BARBER ID TESTS ====================

    @Test
    @DisplayName("Should return barber breaks when admin")
    void getBreaksByBarberId_AsAdmin_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/barber-breaks/barber/" + barber.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].startTime").value("12:00:00"))
                .andExpect(jsonPath("$[0].endTime").value("13:00:00"))
                .andExpect(jsonPath("$[0].label").value("Lunch Break"));
    }

    @Test
    @DisplayName("Should return barber breaks when barber")
    void getBreaksByBarberId_AsBarber_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/barber-breaks/barber/" + barber.getId())
                        .header("Authorization", "Bearer " + barberToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Should reject get breaks when customer")
    void getBreaksByBarberId_AsCustomer_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/barber-breaks/barber/" + barber.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return empty list for barber with no breaks")
    void getBreaksByBarberId_NoBreaks_ReturnsEmptyList() throws Exception {
        // Create another barber with no breaks
        User anotherBarberUser = new User();
        anotherBarberUser.setFirstName("Another");
        anotherBarberUser.setLastName("Barber");
        anotherBarberUser.setEmail("another@test.com");
        anotherBarberUser.setPasswordHash("hashedpassword");
        anotherBarberUser.setPhone("+353874444444");
        anotherBarberUser.setRole(User.Role.BARBER);
        anotherBarberUser.setBusiness(business);
        anotherBarberUser = userRepository.save(anotherBarberUser);

        Barber anotherBarber = new Barber();
        anotherBarber.setUser(anotherBarberUser);
        anotherBarber.setActive(true);
        anotherBarber.setBusiness(business);
        anotherBarber = barberRepository.save(anotherBarber);

        mockMvc.perform(get("/api/barber-breaks/barber/" + anotherBarber.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== DELETE BREAK TESTS ====================

    @Test
    @DisplayName("Should successfully delete break when admin")
    void deleteBreak_AsAdmin_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/barber-breaks/" + barberBreak.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/barber-breaks/barber/" + barber.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should successfully delete break when barber")
    void deleteBreak_AsBarber_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/barber-breaks/" + barberBreak.getId())
                        .header("Authorization", "Bearer " + barberToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should reject delete break when customer")
    void deleteBreak_AsCustomer_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/barber-breaks/" + barberBreak.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return not found when deleting non-existent break")
    void deleteBreak_NonExistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/barber-breaks/99999")
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Business-Slug", business.getSlug()))
                .andExpect(status().isNotFound());
    }
}
