package com.trim.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trim.booking.config.JwtUtil;
import com.trim.booking.dto.service.ServiceRequest;
import com.trim.booking.entity.ServiceCategory;
import com.trim.booking.entity.ServiceOffered;
import com.trim.booking.entity.User;
import com.trim.booking.repository.ServiceCategoryRepository;
import com.trim.booking.repository.ServiceRepository;
import com.trim.booking.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ServiceController
 *
 * Tests cover:
 * - Create service (admin only)
 * - Update service (admin only)
 * - Get all services (authenticated)
 * - Get active services (authenticated)
 * - Get service by ID (authenticated)
 * - Deactivate service (admin only)
 * - Delete service (admin only)
 * - Authorization checks
 * - Validation errors
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceCategoryRepository categoryRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;

    private User admin;
    private User customer;
    private ServiceCategory category;
    private ServiceOffered service;
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
        serviceRepository.deleteAll();
        categoryRepository.deleteAll();
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
    }

    // ==================== CREATE SERVICE TESTS ====================

    @Test
    @DisplayName("Should successfully create a service when admin")
    void createService_AsAdmin_ReturnsCreated() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setName("Premium Haircut");
        request.setDescription("Deluxe haircut with styling");
        request.setDurationMinutes(45);
        request.setPrice(new BigDecimal("40.00"));
        request.setDepositPercentage(25);
        request.setCategoryId(category.getId());

        mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Premium Haircut"))
                .andExpect(jsonPath("$.description").value("Deluxe haircut with styling"))
                .andExpect(jsonPath("$.durationMinutes").value(45))
                .andExpect(jsonPath("$.price").value(40.00))
                .andExpect(jsonPath("$.depositPercentage").value(25))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should reject create service when not admin")
    void createService_AsCustomer_ReturnsForbidden() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setName("Premium Haircut");
        request.setDurationMinutes(45);
        request.setPrice(new BigDecimal("40.00"));
        request.setDepositPercentage(25);
        request.setCategoryId(category.getId());

        mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject create service without authentication")
    void createService_WithoutAuth_ReturnsForbidden() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setName("Premium Haircut");
        request.setDurationMinutes(45);
        request.setPrice(new BigDecimal("40.00"));
        request.setDepositPercentage(25);
        request.setCategoryId(category.getId());

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject create service with missing required fields")
    void createService_WithMissingFields_ReturnsBadRequest() throws Exception {
        ServiceRequest request = new ServiceRequest();
        // Missing all required fields

        mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject create service with negative price")
    void createService_WithNegativePrice_ReturnsBadRequest() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setName("Invalid Service");
        request.setDurationMinutes(30);
        request.setPrice(new BigDecimal("-10.00"));
        request.setDepositPercentage(20);
        request.setCategoryId(category.getId());

        mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject create service with negative duration")
    void createService_WithNegativeDuration_ReturnsBadRequest() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setName("Invalid Service");
        request.setDurationMinutes(-30);
        request.setPrice(new BigDecimal("25.00"));
        request.setDepositPercentage(20);
        request.setCategoryId(category.getId());

        mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== UPDATE SERVICE TESTS ====================

    @Test
    @DisplayName("Should successfully update service when admin")
    void updateService_AsAdmin_ReturnsOk() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setName("Updated Haircut");
        request.setDescription("Updated description");
        request.setDurationMinutes(40);
        request.setPrice(new BigDecimal("30.00"));
        request.setDepositPercentage(25);
        request.setCategoryId(category.getId());

        mockMvc.perform(put("/api/services/" + service.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Haircut"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.durationMinutes").value(40))
                .andExpect(jsonPath("$.price").value(30.00));
    }

    @Test
    @DisplayName("Should reject update service when not admin")
    void updateService_AsCustomer_ReturnsForbidden() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setName("Updated Haircut");
        request.setDurationMinutes(40);
        request.setPrice(new BigDecimal("30.00"));
        request.setDepositPercentage(25);
        request.setCategoryId(category.getId());

        mockMvc.perform(put("/api/services/" + service.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return not found when updating non-existent service")
    void updateService_NonExistentId_ReturnsNotFound() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setName("Updated Haircut");
        request.setDurationMinutes(40);
        request.setPrice(new BigDecimal("30.00"));
        request.setDepositPercentage(25);
        request.setCategoryId(category.getId());

        mockMvc.perform(put("/api/services/99999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ==================== GET ALL SERVICES TESTS ====================

    @Test
    @DisplayName("Should return all services when authenticated")
    void getAllServices_WithAuth_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/services")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").value("Standard Haircut"));
    }

    @Test
    @DisplayName("Should return all services including inactive ones")
    void getAllServices_IncludesInactive() throws Exception {
        // Deactivate the service
        service.setActive(false);
        serviceRepository.save(service);

        mockMvc.perform(get("/api/services")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ==================== GET ACTIVE SERVICES TESTS ====================

    @Test
    @DisplayName("Should return only active services")
    void getActiveServices_ReturnsOnlyActive() throws Exception {
        mockMvc.perform(get("/api/services/active")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @DisplayName("Should not return inactive services in active list")
    void getActiveServices_ExcludesInactive() throws Exception {
        // Deactivate the service
        service.setActive(false);
        serviceRepository.save(service);

        mockMvc.perform(get("/api/services/active")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== GET SERVICE BY ID TESTS ====================

    @Test
    @DisplayName("Should return service by ID")
    void getServiceById_ExistingId_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/services/" + service.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(service.getId()))
                .andExpect(jsonPath("$.name").value("Standard Haircut"))
                .andExpect(jsonPath("$.durationMinutes").value(30))
                .andExpect(jsonPath("$.price").value(25.00))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should return not found for non-existent service ID")
    void getServiceById_NonExistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/services/99999")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());
    }

    // ==================== DEACTIVATE SERVICE TESTS ====================

    @Test
    @DisplayName("Should successfully deactivate service when admin")
    void deactivateService_AsAdmin_ReturnsNoContent() throws Exception {
        mockMvc.perform(patch("/api/services/" + service.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify service is deactivated
        mockMvc.perform(get("/api/services/" + service.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Should reject deactivate service when not admin")
    void deactivateService_AsCustomer_ReturnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/services/" + service.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return not found when deactivating non-existent service")
    void deactivateService_NonExistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(patch("/api/services/99999/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE SERVICE TESTS ====================

    @Test
    @DisplayName("Should successfully delete service when admin")
    void deleteService_AsAdmin_ReturnsNoContent() throws Exception {
        Long serviceIdToDelete = service.getId();

        mockMvc.perform(delete("/api/services/" + serviceIdToDelete)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify service is deleted
        mockMvc.perform(get("/api/services/" + serviceIdToDelete)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should reject delete service when not admin")
    void deleteService_AsCustomer_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/services/" + service.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return no content when deleting non-existent service")
    void deleteService_NonExistentId_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/services/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}

