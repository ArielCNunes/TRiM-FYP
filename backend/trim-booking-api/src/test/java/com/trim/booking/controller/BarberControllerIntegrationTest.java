package com.trim.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trim.booking.config.JwtUtil;
import com.trim.booking.dto.barber.CreateBarberRequest;
import com.trim.booking.dto.barber.UpdateBarberRequest;
import com.trim.booking.entity.Barber;
import com.trim.booking.entity.User;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BarberController
 *
 * Tests cover:
 * - Create barber (admin only)
 * - Update barber (admin only)
 * - Get all barbers (public)
 * - Get active barbers (public)
 * - Get barber by ID (public)
 * - Deactivate barber (admin only)
 * - Reactivate barber (admin only)
 * - Delete barber (admin only)
 * - Authorization checks
 * - Validation errors
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BarberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;

    private User admin;
    private User customer;
    private User barberUser;
    private Barber barber;
    private String adminToken;
    private String customerToken;

    @BeforeAll
    void setupOnce() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUp() {
        // Clean up before each test
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
        barber.setBio("Expert barber with 10 years experience");
        barber.setProfileImageUrl("https://example.com/image.jpg");
        barber.setActive(true);
        barber = barberRepository.save(barber);

        // Generate JWT tokens
        adminToken = jwtUtil.generateToken(admin.getEmail(), "ADMIN", admin.getId());
        customerToken = jwtUtil.generateToken(customer.getEmail(), "CUSTOMER", customer.getId());
    }

    // ==================== CREATE BARBER TESTS ====================

    @Test
    @DisplayName("Should successfully create a barber when admin")
    void createBarber_AsAdmin_ReturnsCreated() throws Exception {
        CreateBarberRequest request = new CreateBarberRequest();
        request.setFirstName("New");
        request.setLastName("Barber");
        request.setEmail("newbarber@test.com");
        request.setPhone("+353874444444");
        request.setPassword("password123");
        request.setBio("New barber bio");
        request.setProfileImageUrl("https://example.com/new.jpg");

        mockMvc.perform(post("/api/barbers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.bio").value("New barber bio"))
                .andExpect(jsonPath("$.profileImageUrl").value("https://example.com/new.jpg"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should reject create barber when not admin")
    void createBarber_AsCustomer_ReturnsForbidden() throws Exception {
        CreateBarberRequest request = new CreateBarberRequest();
        request.setFirstName("New");
        request.setLastName("Barber");
        request.setEmail("newbarber@test.com");
        request.setPhone("+353874444444");
        request.setPassword("password123");

        mockMvc.perform(post("/api/barbers")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject create barber without authentication")
    void createBarber_WithoutAuth_ReturnsForbidden() throws Exception {
        CreateBarberRequest request = new CreateBarberRequest();
        request.setFirstName("New");
        request.setLastName("Barber");
        request.setEmail("newbarber@test.com");
        request.setPhone("+353874444444");
        request.setPassword("password123");

        mockMvc.perform(post("/api/barbers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject create barber with missing required fields")
    void createBarber_WithMissingFields_ReturnsBadRequest() throws Exception {
        CreateBarberRequest request = new CreateBarberRequest();
        // Missing all required fields

        mockMvc.perform(post("/api/barbers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject create barber with invalid email")
    void createBarber_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        CreateBarberRequest request = new CreateBarberRequest();
        request.setFirstName("New");
        request.setLastName("Barber");
        request.setEmail("invalid-email");
        request.setPhone("+353874444444");
        request.setPassword("password123");

        mockMvc.perform(post("/api/barbers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject create barber with short password")
    void createBarber_WithShortPassword_ReturnsBadRequest() throws Exception {
        CreateBarberRequest request = new CreateBarberRequest();
        request.setFirstName("New");
        request.setLastName("Barber");
        request.setEmail("newbarber@test.com");
        request.setPhone("+353874444444");
        request.setPassword("short"); // Less than 8 characters

        mockMvc.perform(post("/api/barbers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== UPDATE BARBER TESTS ====================

    @Test
    @DisplayName("Should successfully update barber when admin")
    void updateBarber_AsAdmin_ReturnsOk() throws Exception {
        UpdateBarberRequest request = new UpdateBarberRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setBio("Updated bio");

        mockMvc.perform(put("/api/barbers/" + barber.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Updated bio"));
    }

    @Test
    @DisplayName("Should reject update barber when not admin")
    void updateBarber_AsCustomer_ReturnsForbidden() throws Exception {
        UpdateBarberRequest request = new UpdateBarberRequest();
        request.setBio("Updated bio");

        mockMvc.perform(put("/api/barbers/" + barber.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return not found when updating non-existent barber")
    void updateBarber_NonExistentId_ReturnsNotFound() throws Exception {
        UpdateBarberRequest request = new UpdateBarberRequest();
        request.setBio("Updated bio");

        mockMvc.perform(put("/api/barbers/99999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ==================== GET ALL BARBERS TESTS ====================

    @Test
    @DisplayName("Should return all barbers when authenticated")
    void getAllBarbers_WithAuth_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/barbers")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    @DisplayName("Should return all barbers including inactive ones")
    void getAllBarbers_IncludesInactive() throws Exception {
        // Deactivate the barber
        barber.setActive(false);
        barberRepository.save(barber);

        mockMvc.perform(get("/api/barbers")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ==================== GET ACTIVE BARBERS TESTS ====================

    @Test
    @DisplayName("Should return only active barbers")
    void getActiveBarbers_ReturnsOnlyActive() throws Exception {
        mockMvc.perform(get("/api/barbers/active")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @DisplayName("Should not return inactive barbers in active list")
    void getActiveBarbers_ExcludesInactive() throws Exception {
        // Deactivate the barber
        barber.setActive(false);
        barberRepository.save(barber);

        mockMvc.perform(get("/api/barbers/active")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== GET BARBER BY ID TESTS ====================

    @Test
    @DisplayName("Should return barber by ID")
    void getBarberById_ExistingId_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/barbers/" + barber.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(barber.getId()))
                .andExpect(jsonPath("$.bio").value("Expert barber with 10 years experience"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should return not found for non-existent barber ID")
    void getBarberById_NonExistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/barbers/99999")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());
    }

    // ==================== DEACTIVATE BARBER TESTS ====================

    @Test
    @DisplayName("Should successfully deactivate barber when admin")
    void deactivateBarber_AsAdmin_ReturnsNoContent() throws Exception {
        mockMvc.perform(patch("/api/barbers/" + barber.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify barber is deactivated
        mockMvc.perform(get("/api/barbers/" + barber.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Should reject deactivate barber when not admin")
    void deactivateBarber_AsCustomer_ReturnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/barbers/" + barber.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return not found when deactivating non-existent barber")
    void deactivateBarber_NonExistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(patch("/api/barbers/99999/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ==================== REACTIVATE BARBER TESTS ====================

    @Test
    @DisplayName("Should successfully reactivate barber when admin")
    void reactivateBarber_AsAdmin_ReturnsNoContent() throws Exception {
        // First deactivate the barber
        barber.setActive(false);
        barberRepository.save(barber);

        mockMvc.perform(patch("/api/barbers/" + barber.getId() + "/activate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify barber is reactivated
        mockMvc.perform(get("/api/barbers/" + barber.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should reject reactivate barber when not admin")
    void reactivateBarber_AsCustomer_ReturnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/barbers/" + barber.getId() + "/activate")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return not found when reactivating non-existent barber")
    void reactivateBarber_NonExistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(patch("/api/barbers/99999/activate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE BARBER TESTS ====================

    @Test
    @DisplayName("Should successfully delete barber when admin")
    void deleteBarber_AsAdmin_ReturnsNoContent() throws Exception {
        Long barberIdToDelete = barber.getId();

        mockMvc.perform(delete("/api/barbers/" + barberIdToDelete)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify barber is deleted
        mockMvc.perform(get("/api/barbers/" + barberIdToDelete)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should reject delete barber when not admin")
    void deleteBarber_AsCustomer_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/barbers/" + barber.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return no content when deleting non-existent barber (service doesn't validate)")
    void deleteBarber_NonExistentId_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/barbers/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}

