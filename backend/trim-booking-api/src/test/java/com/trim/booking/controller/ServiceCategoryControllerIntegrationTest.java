package com.trim.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trim.booking.config.JwtUtil;
import com.trim.booking.dto.service.CategoryRequest;
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
 * Integration tests for ServiceCategoryController
 *
 * Tests cover:
 * - Create category (admin only)
 * - Update category (admin only)
 * - Get all categories (authenticated)
 * - Get categories with services (authenticated)
 * - Get category by ID (authenticated)
 * - Deactivate category (admin only)
 * - Authorization checks
 * - Validation errors
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceCategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceCategoryRepository categoryRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;

    private User admin;
    private User customer;
    private ServiceCategory category;
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

        // Generate JWT tokens
        adminToken = jwtUtil.generateToken(admin.getEmail(), "ADMIN", admin.getId());
        customerToken = jwtUtil.generateToken(customer.getEmail(), "CUSTOMER", customer.getId());
    }

    // ==================== CREATE CATEGORY TESTS ====================

    @Test
    @DisplayName("Should successfully create a category when admin")
    void createCategory_AsAdmin_ReturnsCreated() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Beard Trims");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Beard Trims"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should reject create category when not admin")
    void createCategory_AsCustomer_ReturnsForbidden() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Beard Trims");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject create category without authentication")
    void createCategory_WithoutAuth_ReturnsForbidden() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Beard Trims");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject create category with blank name")
    void createCategory_WithBlankName_ReturnsBadRequest() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== UPDATE CATEGORY TESTS ====================

    @Test
    @DisplayName("Should successfully update category when admin")
    void updateCategory_AsAdmin_ReturnsOk() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Category Name");

        mockMvc.perform(put("/api/categories/" + category.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category Name"));
    }

    @Test
    @DisplayName("Should reject update category when not admin")
    void updateCategory_AsCustomer_ReturnsForbidden() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Category Name");

        mockMvc.perform(put("/api/categories/" + category.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return not found when updating non-existent category")
    void updateCategory_NonExistentId_ReturnsNotFound() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Category Name");

        mockMvc.perform(put("/api/categories/99999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ==================== GET ALL CATEGORIES TESTS ====================

    @Test
    @DisplayName("Should return all categories when authenticated")
    void getAllCategories_WithAuth_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").value("Haircuts"));
    }

    // ==================== GET CATEGORIES WITH SERVICES TESTS ====================

    @Test
    @DisplayName("Should return categories with their services")
    void getCategoriesWithServices_ReturnsOk() throws Exception {
        // Add a service to the category
        ServiceOffered service = new ServiceOffered();
        service.setName("Standard Haircut");
        service.setDescription("Basic haircut");
        service.setDurationMinutes(30);
        service.setPrice(new BigDecimal("25.00"));
        service.setDepositPercentage(20);
        service.setActive(true);
        service.setCategory(category);
        serviceRepository.save(service);

        mockMvc.perform(get("/api/categories/with-services")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name").value("Haircuts"))
                .andExpect(jsonPath("$[0].services", hasSize(1)))
                .andExpect(jsonPath("$[0].services[0].name").value("Standard Haircut"));
    }

    @Test
    @DisplayName("Should return empty services array for category without services")
    void getCategoriesWithServices_NoServices_ReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/categories/with-services")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].services", hasSize(0)));
    }

    // ==================== GET CATEGORY BY ID TESTS ====================

    @Test
    @DisplayName("Should return category by ID")
    void getCategoryById_ExistingId_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/categories/" + category.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Haircuts"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should return not found for non-existent category ID")
    void getCategoryById_NonExistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/categories/99999")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());
    }

    // ==================== DEACTIVATE CATEGORY TESTS ====================

    @Test
    @DisplayName("Should successfully deactivate category when admin")
    void deactivateCategory_AsAdmin_ReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/categories/" + category.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Should reject deactivate category when not admin")
    void deactivateCategory_AsCustomer_ReturnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/categories/" + category.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return not found when deactivating non-existent category")
    void deactivateCategory_NonExistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(patch("/api/categories/99999/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}

