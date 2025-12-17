package com.trim.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trim.booking.config.JwtUtil;
import com.trim.booking.entity.Barber;
import com.trim.booking.entity.BarberAvailability;
import com.trim.booking.entity.User;
import com.trim.booking.repository.BarberAvailabilityRepository;
import com.trim.booking.repository.BarberRepository;
import com.trim.booking.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BarberAvailabilityController
 *
 * Tests cover:
 * - Set availability (admin/barber only)
 * - Update availability (admin/barber only)
 * - Get barber availability (authenticated)
 * - Get all availability (authenticated)
 * - Delete availability (admin/barber only)
 * - Authorization checks
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BarberAvailabilityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private BarberAvailabilityRepository availabilityRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;

    private User admin;
    private User customer;
    private User barberUser;
    private Barber barber;
    private BarberAvailability availability;
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
        availabilityRepository.deleteAll();
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

        // Create availability
        availability = new BarberAvailability();
        availability.setBarber(barber);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setIsAvailable(true);
        availability = availabilityRepository.save(availability);

        // Generate JWT tokens
        adminToken = jwtUtil.generateToken(admin.getEmail(), "ADMIN", admin.getId());
        customerToken = jwtUtil.generateToken(customer.getEmail(), "CUSTOMER", customer.getId());
        barberToken = jwtUtil.generateToken(barberUser.getEmail(), "BARBER", barberUser.getId());
    }

    // ==================== SET AVAILABILITY TESTS ====================

    @Test
    @DisplayName("Should successfully set availability when admin")
    void setAvailability_AsAdmin_ReturnsCreated() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("barberId", barber.getId());
        request.put("dayOfWeek", "TUESDAY");
        request.put("startTime", "10:00");
        request.put("endTime", "18:00");
        request.put("isAvailable", true);

        mockMvc.perform(post("/api/barber-availability")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.dayOfWeek").value("TUESDAY"))
                .andExpect(jsonPath("$.startTime").value("10:00:00"))
                .andExpect(jsonPath("$.endTime").value("18:00:00"))
                .andExpect(jsonPath("$.isAvailable").value(true));
    }

    @Test
    @DisplayName("Should successfully set availability when barber")
    void setAvailability_AsBarber_ReturnsCreated() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("barberId", barber.getId());
        request.put("dayOfWeek", "WEDNESDAY");
        request.put("startTime", "08:00");
        request.put("endTime", "16:00");
        request.put("isAvailable", true);

        mockMvc.perform(post("/api/barber-availability")
                        .header("Authorization", "Bearer " + barberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dayOfWeek").value("WEDNESDAY"));
    }

    @Test
    @DisplayName("Should reject set availability when customer")
    void setAvailability_AsCustomer_ReturnsForbidden() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("barberId", barber.getId());
        request.put("dayOfWeek", "TUESDAY");
        request.put("startTime", "10:00");
        request.put("endTime", "18:00");
        request.put("isAvailable", true);

        mockMvc.perform(post("/api/barber-availability")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject set availability without authentication")
    void setAvailability_WithoutAuth_ReturnsForbidden() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("barberId", barber.getId());
        request.put("dayOfWeek", "TUESDAY");
        request.put("startTime", "10:00");
        request.put("endTime", "18:00");
        request.put("isAvailable", true);

        mockMvc.perform(post("/api/barber-availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return not found when setting availability for non-existent barber")
    void setAvailability_NonExistentBarber_ReturnsNotFound() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("barberId", 99999);
        request.put("dayOfWeek", "TUESDAY");
        request.put("startTime", "10:00");
        request.put("endTime", "18:00");
        request.put("isAvailable", true);

        mockMvc.perform(post("/api/barber-availability")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ==================== UPDATE AVAILABILITY TESTS ====================

    @Test
    @DisplayName("Should successfully update availability when admin")
    void updateAvailability_AsAdmin_ReturnsOk() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("startTime", "10:00");
        request.put("endTime", "19:00");

        mockMvc.perform(put("/api/barber-availability/" + availability.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("10:00:00"))
                .andExpect(jsonPath("$.endTime").value("19:00:00"));
    }

    @Test
    @DisplayName("Should successfully update availability when barber")
    void updateAvailability_AsBarber_ReturnsOk() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("isAvailable", false);

        mockMvc.perform(put("/api/barber-availability/" + availability.getId())
                        .header("Authorization", "Bearer " + barberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false));
    }

    @Test
    @DisplayName("Should reject update availability when customer")
    void updateAvailability_AsCustomer_ReturnsForbidden() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("startTime", "10:00");

        mockMvc.perform(put("/api/barber-availability/" + availability.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return not found when updating non-existent availability")
    void updateAvailability_NonExistentId_ReturnsNotFound() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("startTime", "10:00");

        mockMvc.perform(put("/api/barber-availability/99999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ==================== GET BARBER AVAILABILITY TESTS ====================

    @Test
    @DisplayName("Should return barber availability when authenticated")
    void getBarberAvailability_WithAuth_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/barber-availability/barber/" + barber.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$[0].endTime").value("17:00:00"));
    }

    @Test
    @DisplayName("Should return empty list for barber with no availability")
    void getBarberAvailability_NoAvailability_ReturnsEmptyList() throws Exception {
        // Create another barber with no availability
        User anotherBarberUser = new User();
        anotherBarberUser.setFirstName("Another");
        anotherBarberUser.setLastName("Barber");
        anotherBarberUser.setEmail("another@test.com");
        anotherBarberUser.setPasswordHash("hashedpassword");
        anotherBarberUser.setPhone("+353874444444");
        anotherBarberUser.setRole(User.Role.BARBER);
        anotherBarberUser = userRepository.save(anotherBarberUser);

        Barber anotherBarber = new Barber();
        anotherBarber.setUser(anotherBarberUser);
        anotherBarber.setActive(true);
        anotherBarber = barberRepository.save(anotherBarber);

        mockMvc.perform(get("/api/barber-availability/barber/" + anotherBarber.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== GET ALL AVAILABILITY TESTS ====================

    @Test
    @DisplayName("Should return all availability when authenticated")
    void getAllAvailability_WithAuth_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/barber-availability")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ==================== DELETE AVAILABILITY TESTS ====================

    @Test
    @DisplayName("Should successfully delete availability when admin")
    void deleteAvailability_AsAdmin_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/barber-availability/" + availability.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/barber-availability/barber/" + barber.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should successfully delete availability when barber")
    void deleteAvailability_AsBarber_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/barber-availability/" + availability.getId())
                        .header("Authorization", "Bearer " + barberToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should reject delete availability when customer")
    void deleteAvailability_AsCustomer_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/barber-availability/" + availability.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return no content when deleting non-existent availability")
    void deleteAvailability_NonExistentId_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/barber-availability/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}

